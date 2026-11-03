# Feature migration matrix — v0.6.0

| Capability | v0.6.0 status | Notes |
|---|---|---|
| Text-to-Figure | Implemented (core) | Prompt-driven editable SVG generation |
| Paper-to-Figure | Partial | PDF text extraction; no scanned-PDF OCR |
| Iterative refinement | Implemented (core) | Critic/revision loop |
| SVG output and source editing | Implemented | SVG preview and source editor |
| Provider chat APIs | Implemented in source | Gemini GenerateContent + OpenAI-compatible chat APIs; real account/model testing remains |
| Raster image generation | Implemented adapter | Gemini image response and OpenAI Images-compatible endpoint; separate image model ID required |
| PNG save | Implemented | Converts returned raster image to PNG before saving |
| SVG object inspector | Partial | Lists common SVG primitives and changes one selected element's fill |
| Reference image | Partial | Vision input supported for compatible chat models |
| SAM3 segmentation | Not implemented | Requires SAM3 model/service and response adapter |
| RMBG background removal | Not implemented | Requires model/service and response adapter |
| Automatic icon detection/cropping | Not implemented | No detector or asset library yet |
| Raster-to-vector tracing | Not implemented | No tracing engine yet |
| SVG template reconstruction | Not implemented | Geometry/layout reconstruction not implemented |
| Native mxGraph cells | Not implemented | draw.io export still embeds the SVG as one image |
| Full node-level visual editor | Not implemented | No drag, resize, connector, group, typography or geometry editing |
| Scanned-PDF OCR | Not implemented | Text-based PDF extraction only |
| High-resolution SVG-to-PNG/PDF export | Not implemented | Generated raster image can be saved; SVG rendering export is separate |
