package com.example.model

object ModelCatalog {

    val curatedModels: List<ModelItem> = listOf(
        ModelItem(
            id = "smollm2-135m-instruct",
            name = "SmolLM2 135M",
            parameterCount = "135 Million",
            quantization = "Q4_K_M (4-bit medium)",
            downloadSizeBytes = 102L * 1024 * 1024,
            requiredRamBytes = 512L * 1024 * 1024,
            minStorageBytes = 250L * 1024 * 1024,
            contextSize = 2048,
            tagline = "Ultra-Lightweight & Instant",
            description = "Extremely fast, compact on-device LLM built for quick replies, definitions, and basic assistant tasks with virtually zero memory impact.",
            targetUseCases = listOf("Quick answers", "Everyday tasks", "Low RAM devices", "Brainstorming"),
            downloadUrl = "https://huggingface.co/HuggingFaceTB/SmolLM2-135M-Instruct-GGUF/resolve/main/smollm2-135m-instruct-q4_k_m.gguf",
            sha256Checksum = "e57849e7943d043b35520e54d80dcb8668c2d2e1189fe000ce8feae4ee936df3",
            filename = "smollm2-135m-instruct-q4_k_m.gguf"
        ),
        ModelItem(
            id = "smollm2-360m-instruct",
            name = "SmolLM2 360M",
            parameterCount = "360 Million",
            quantization = "Q4_K_M (4-bit medium)",
            downloadSizeBytes = 245L * 1024 * 1024,
            requiredRamBytes = 900L * 1024 * 1024,
            minStorageBytes = 500L * 1024 * 1024,
            contextSize = 2048,
            tagline = "Compact & Agile General Assistant",
            description = "A well-balanced mobile model delivering high coherence, summary synthesis, and conversational clarity while consuming under 1 GB RAM.",
            targetUseCases = listOf("General assistant", "Summarization", "Drafting text", "Studying"),
            downloadUrl = "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q4_k_m.gguf",
            sha256Checksum = "d1b54a37b34e56fa5bbdecdba128a38b21c430fe8a688b1397b9148d2bc00bfa",
            filename = "smollm2-360m-instruct-q4_k_m.gguf"
        ),
        ModelItem(
            id = "qwen2.5-0.5b-instruct",
            name = "Qwen 2.5 0.5B",
            parameterCount = "490 Million",
            quantization = "Q4_K_M (4-bit medium)",
            downloadSizeBytes = 398L * 1024 * 1024,
            requiredRamBytes = 1200L * 1024 * 1024,
            minStorageBytes = 800L * 1024 * 1024,
            contextSize = 4096,
            tagline = "Versatile Multilingual Specialist",
            description = "High-efficiency multilingual model with strong logical consistency, code formatting, and translation capabilities in 29+ languages.",
            targetUseCases = listOf("Translation", "Coding", "Studying", "Logical reasoning"),
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            sha256Checksum = "55ac40ff8d43890f5c186980db4ca364a2753a479ffda6f1945f3c051a8eb084",
            filename = "qwen2.5-0.5b-instruct-q4_k_m.gguf"
        ),
        ModelItem(
            id = "llama-3.2-1b-instruct",
            name = "Llama 3.2 1B",
            parameterCount = "1.23 Billion",
            quantization = "Q4_K_M (4-bit medium)",
            downloadSizeBytes = 848L * 1024 * 1024,
            requiredRamBytes = 2200L * 1024 * 1024,
            minStorageBytes = 1600L * 1024 * 1024,
            contextSize = 4096,
            tagline = "Flagship Compact Intelligence",
            description = "Meta's official lightweight Llama model engineered specifically for on-device applications. Excellent instruction following and writing skills.",
            targetUseCases = listOf("General assistant", "Creative writing", "Complex synthesis", "Coding"),
            downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
            sha256Checksum = "4c382f7d084ee0a9058b8f2d59ae186dbfca3c69c6cf2c7e0f8f946761081512",
            filename = "llama-3.2-1b-instruct-q4_k_m.gguf"
        ),
        ModelItem(
            id = "gemma-2-2b-it",
            name = "Gemma 2 2B",
            parameterCount = "2.61 Billion",
            quantization = "Q4_K_M (4-bit medium)",
            downloadSizeBytes = 1680L * 1024 * 1024,
            requiredRamBytes = 3600L * 1024 * 1024,
            minStorageBytes = 2800L * 1024 * 1024,
            contextSize = 4096,
            tagline = "Deep Knowledge & Analysis",
            description = "Google's advanced compact architecture delivering state-of-the-art reasoning and depth. Best suited for modern devices with 6 GB+ RAM.",
            targetUseCases = listOf("Deep reasoning", "Academic research", "Creative writing", "STEM"),
            downloadUrl = "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf",
            sha256Checksum = "71f76e19e075c3db0852e071746ad46944bf9b2f6ef1e5d71c4c1a59bf40ea46",
            filename = "gemma-2-2b-it-q4_k_m.gguf"
        ),
        ModelItem(
            id = "phi-3.5-mini-instruct",
            name = "Phi-3.5 Mini",
            parameterCount = "3.82 Billion",
            quantization = "Q4_K_M (4-bit medium)",
            downloadSizeBytes = 2240L * 1024 * 1024,
            requiredRamBytes = 4900L * 1024 * 1024,
            minStorageBytes = 3800L * 1024 * 1024,
            contextSize = 4096,
            tagline = "Heavyweight Logic Engine",
            description = "Dense multi-billion parameter model capable of graduate-level reasoning, complex code generation, and multi-step deduction on 8 GB+ RAM hardware.",
            targetUseCases = listOf("Advanced coding", "Mathematics", "Long document analysis", "Technical problem solving"),
            downloadUrl = "https://huggingface.co/bartowski/Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf",
            sha256Checksum = "982e0a2944b76dfb378eb87541f5a50785eaae7b4618e9508bc0fa55b9e07f61",
            filename = "phi-3.5-mini-instruct-q4_k_m.gguf"
        )
    )
}
