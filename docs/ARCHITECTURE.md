# Architecture

## Principles

1. Clean-room Android implementation: Kotlin + Jetpack Compose; no old Android client code and no copied AutoFigure Python/Next.js application implementation.
2. Preserve the reference project's functional behavior: methodology understanding, generation, judge/review, iterative refinement, enhancement, SVG/mxGraph output.
3. Keep the phone as the orchestrator and artifact workspace. Model inference is delegated to user-configured providers; no mandatory VPS.
4. Keep vector output first-class. A PNG is a preview, never a substitute for the editable SVG source.

## Layers

- `ui`: Compose screens, task controls, preview and export.
- `engine`: provider transport, generation/review loop, document extraction, format conversion and safety checks.
- `data`: local project/artifact persistence.

## Pipeline target

Input text/PDF/reference image -> methodology extraction -> structured figure plan -> initial generation -> structural validation -> independent critique -> revision loop -> optional image enhancement / segmentation / background removal -> vector reconstruction and assembly -> SVG editor -> SVG, draw.io XML, PNG and PDF export.

## Provider strategy

The current transport supports OpenAI-compatible chat-completions endpoints. Gemini's OpenAI-compatible endpoint, OpenRouter, and other compatible gateways can be configured with their appropriate base URL/model/key. Native Gemini Files/Images and provider-specific image generation require dedicated adapters and are not falsely treated as chat-completions features.
