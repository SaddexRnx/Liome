#include <jni.h>
#include <android/log.h>
#include <algorithm>
#include <cstring>
#include <memory>
#include <string>
#include <vector>
#include "llama.h"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "LocalMindJNI", __VA_ARGS__)

struct Handle {
    llama_model *model{};
    llama_context *ctx{};
    const llama_vocab *vocab{};
    llama_token pendingToken{0};
    int32_t nextPosition{0};
    bool hasPendingToken{false};
};
static Handle * asHandle(jlong h) { return reinterpret_cast<Handle *>(h); }

extern "C" JNIEXPORT jboolean JNICALL Java_com_example_engine_NativeLlamaBridge_nativeInit(JNIEnv *, jobject) {
    llama_backend_init();
    return JNI_TRUE;
}

extern "C" JNIEXPORT jlong JNICALL Java_com_example_engine_NativeLlamaBridge_nativeLoadModel(JNIEnv *env, jobject, jstring path, jint threads, jint context, jint, jboolean mmap, jboolean mlock) {
    const char *cpath = env->GetStringUTFChars(path, nullptr);
    // mmap/mlock were removed from llama_model_params in newer llama.cpp
    // releases; the current backend chooses the platform-safe defaults.
    llama_model_params mp = llama_model_default_params();
    llama_model *model = llama_model_load_from_file(cpath, mp);
    env->ReleaseStringUTFChars(path, cpath);
    if (!model) return 0;
    llama_context_params cp = llama_context_default_params();
    cp.n_ctx = static_cast<uint32_t>(context);
    cp.n_batch = std::min<uint32_t>(512, cp.n_ctx);
    cp.n_threads = std::max(1, static_cast<int>(threads));
    cp.n_threads_batch = cp.n_threads;
    llama_context *ctx = llama_init_from_model(model, cp);
    if (!ctx) { llama_model_free(model); return 0; }
    auto *handle = new Handle{model, ctx, llama_model_get_vocab(model)};
    return reinterpret_cast<jlong>(handle);
}

extern "C" JNIEXPORT void JNICALL Java_com_example_engine_NativeLlamaBridge_nativeFreeModel(JNIEnv *, jobject, jlong handle) {
    auto *h = asHandle(handle); if (!h) return;
    llama_free(h->ctx); llama_model_free(h->model); delete h;
}

extern "C" JNIEXPORT jintArray JNICALL Java_com_example_engine_NativeLlamaBridge_nativeTokenize(JNIEnv *env, jobject, jlong handle, jstring text) {
    auto *h = asHandle(handle); if (!h) return nullptr;
    const char *input = env->GetStringUTFChars(text, nullptr);
    int n = -llama_tokenize(h->vocab, input, strlen(input), nullptr, 0, true, true);
    std::vector<llama_token> tokens(n);
    llama_tokenize(h->vocab, input, strlen(input), tokens.data(), tokens.size(), true, true);
    env->ReleaseStringUTFChars(text, input);
    jintArray result = env->NewIntArray(n); env->SetIntArrayRegion(result, 0, n, reinterpret_cast<jint *>(tokens.data())); return result;
}

extern "C" JNIEXPORT jboolean JNICALL Java_com_example_engine_NativeLlamaBridge_nativeEval(JNIEnv *env, jobject, jlong handle, jintArray input, jint nPast) {
    auto *h = asHandle(handle); if (!h) return JNI_FALSE;
    jsize n = env->GetArrayLength(input); std::vector<llama_token> tokens(n); env->GetIntArrayRegion(input, 0, n, reinterpret_cast<jint *>(tokens.data()));
    llama_batch batch = llama_batch_init(n, 0, 1);
    for (int i = 0; i < n; ++i) { batch.token[i] = tokens[i]; batch.pos[i] = nPast + i; batch.seq_id[i][0] = 0; batch.n_seq_id[i] = 1; batch.logits[i] = (i == n - 1); }
    bool ok = llama_decode(h->ctx, batch) == 0;
    if (ok) {
        h->nextPosition = nPast + n;
        h->hasPendingToken = false;
    }
    llama_batch_free(batch);
    return ok ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_engine_NativeLlamaBridge_nativeSampleNextToken(JNIEnv *, jobject, jlong handle, jfloat temperature, jfloat topP, jfloat repeatPenalty) {
    auto *h = asHandle(handle); if (!h) return 0;

    // The Kotlin bridge asks for one token at a time. Decode the previously
    // sampled token before sampling the next one so generation advances through
    // the model instead of repeatedly sampling the prompt logits.
    if (h->hasPendingToken) {
        llama_batch batch = llama_batch_init(1, 0, 1);
        batch.token[0] = h->pendingToken;
        batch.pos[0] = h->nextPosition++;
        batch.seq_id[0][0] = 0;
        batch.n_seq_id[0] = 1;
        batch.logits[0] = 1;
        if (llama_decode(h->ctx, batch) != 0) {
            llama_batch_free(batch);
            return 0;
        }
        llama_batch_free(batch);
    }

    auto *sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(topP, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(temperature));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));
    llama_token token = llama_sampler_sample(sampler, h->ctx, -1);
    llama_sampler_free(sampler);
    h->pendingToken = token;
    h->hasPendingToken = true;
    return static_cast<jint>(token);
}

extern "C" JNIEXPORT jstring JNICALL Java_com_example_engine_NativeLlamaBridge_nativeTokenToString(JNIEnv *env, jobject, jlong handle, jint token) {
    auto *h = asHandle(handle); if (!h) return env->NewStringUTF("");
    char buffer[256]; int n = llama_token_to_piece(h->vocab, token, buffer, sizeof(buffer), 0, true); if (n < 0) return env->NewStringUTF("");
    return env->NewStringUTF(std::string(buffer, n).c_str());
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_engine_NativeLlamaBridge_nativeGetContextSize(JNIEnv *, jobject, jlong handle) { auto *h = asHandle(handle); return h ? static_cast<jint>(llama_n_ctx(h->ctx)) : 0; }
