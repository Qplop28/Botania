#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
// [VanillaCopy] particle.fsh with changes

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
    float gs = rand(vec2(int(texCoord0.x * 256), int(texCoord0.y * 512))) * 0.5;
    // texture.rgb and vertexColor deliberately ignored
    vec4 color = vec4(gs, gs, gs, texture(Sampler0, texCoord0).a) * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
        FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
