# AutoFigure Android — independent clean-room rewrite

Version 0.5.0 adds a provider-aware API layer to the independent Android rewrite. The project is based on the functional/algorithmic behavior documented in `ResearAI/AutoFigure`; it does not use the previously uploaded Android project as its code base.

## AI providers

- **Gemini native API**: Google Gemini `models.generateContent` REST schema, including native `system_instruction` and image `inline_data` parts. Default API root: `https://generativelanguage.googleapis.com/v1beta`.
- **OpenAI**: Chat Completions compatible request. Default API root: `https://api.openai.com/v1`.
- **DeepSeek**: OpenAI-compatible Chat Completions. Default API root: `https://api.deepseek.com`.
- **OpenRouter**: OpenAI-compatible Chat Completions. Default API root: `https://openrouter.ai/api/v1`.
- **Custom**: user-supplied OpenAI-compatible base URL and model ID.

The model ID remains user-editable. Provider, base URL, API key and model are saved locally. The selected provider controls request serialization and authentication headers. Gemini uses `x-goog-api-key`; the compatible providers use Bearer authentication.

## Existing functionality

- Jetpack Compose interface and local provider configuration
- Text-to-SVG generation with critic/revision loop
- Reference-image input for models that support vision
- PDF text extraction (not OCR)
- Local project history
- SVG preview, source editing, SVG export and basic draw.io wrapper export
- GitHub Actions Android build workflow

## Build

Open this folder in Android Studio with JDK 17 and Android SDK 35. Sync Gradle and run the `app` configuration. A Gradle wrapper is not included. GitHub Actions can also build the debug APK and publish it as an artifact.

## Important limitations

- The Gemini adapter currently uses the documented `generateContent` REST endpoint. Google now recommends its Interactions API for new agentic workflows; that endpoint is not implemented in this release.
- OpenAI Responses API is not implemented; OpenAI currently uses Chat Completions.
- Custom endpoints must be OpenAI Chat Completions-compatible; arbitrary vendor-specific schemas are not supported.
- Image input only works when the selected model supports vision. Image generation endpoints (e.g. image-generation models) are not integrated; generated scientific figures remain SVG text output.
- SAM segmentation, background removal, automated icon extraction, SVG template reconstruction, native editable mxGraph shapes, OCR, high-resolution raster/PDF rendering, and encrypted API-key storage are not implemented.
- API keys are stored in app-private preferences without Keystore-backed encryption. Use a restricted key with provider-side limits; do not commit keys to source control.

See `docs/IMPLEMENTATION-STATUS.md` and the `licenses/` directory for scope and upstream attribution.


## v0.6.0 additions
- Gemini image generation via native `generateContent` image response parts.
- OpenAI Images API-compatible image generation endpoint.
- Save generated raster output as PNG.
- Lightweight per-element SVG fill-color editing for common SVG primitives.
- See `docs/IMPLEMENTATION-STATUS.md` for verified scope and limitations.
