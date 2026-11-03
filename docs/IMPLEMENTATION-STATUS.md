# Implementation status — v0.6.0

## Implemented in this checkpoint
- All v0.5.0 provider and scientific SVG generation workflows remain.
- Added a raster image-generation adapter for Gemini native `generateContent` image responses and OpenAI Images API-compatible `/images/generations` endpoints.
- Added an in-app image-generation action using the configured provider/model and PNG save through Android's document picker.
- Added a lightweight SVG object inspector for common elements (`rect`, `circle`, `ellipse`, `path`, `line`, `polyline`, `polygon`, `text`) and per-element fill-color editing.
- Added unit tests for SVG element discovery and fill editing.

## Important limitations
- A chat model is not automatically an image-generation model. The user must select a model that supports image output. The OpenAI-compatible image adapter expects a base64 `b64_json` response; not every gateway implements this.
- Gemini image output depends on the selected model's image-generation capability and account/API availability.
- This checkpoint does not yet include SAM3 segmentation, RMBG background removal, automatic icon detection/cropping, raster-to-vector tracing, full SVG geometry reconstruction, native mxGraph cells, full node drag/resize editor, OCR, or production PNG/PDF rendering of SVG.
- The SVG object editor deliberately edits fill color on common SVG elements. It is not yet a full visual canvas and does not yet expose geometry, stroke, typography, group hierarchy, or connector editing.
- API Key storage is still ordinary app preferences, not encrypted.
- Source has not been built in this environment because Android SDK/Gradle are unavailable. Run GitHub Actions/Android Studio before installing.
