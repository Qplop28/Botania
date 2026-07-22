#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

float unpackUnsigned16(float highChannel, float lowChannel) {
    float highByte = floor(highChannel * 255.0 + 0.5);
    float lowByte = floor(lowChannel * 255.0 + 0.5);
    return (highByte * 256.0 + lowByte) / 65535.0;
}

void main() {
    float grainIntensity = unpackUnsigned16(vertexColor.r, vertexColor.g);
    float healthFraction = unpackUnsigned16(vertexColor.b, vertexColor.a);

    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }

    // GameTime is in [0, 1) over course of the day, turn it back into ticks
    float time = GameTime * 24000;

    float s = sin((texCoord0.y + 0.0375) * 100.0 + sin(texCoord0.x * 90.0 - time / 2.0) * healthFraction) * 0.5 + 0.5;
    s = 1.0 - s;
    if(s <= 0.98)
    s = 0;
    s *= 0.4;

    vec3 newColor = vec3(
        min(1, color.r + s),
        min(1, color.g + s),
        min(1, color.b + s)
    );

    if(healthFraction <= 0.2) {
        float flash = (sin(time * 2.0) - 0.8) * 5;
        if(flash > 0) {
            newColor.g = max(0, newColor.g - flash);
            newColor.b = max(0, newColor.b - flash);
        }
    }

    if(grainIntensity > 0) {
        float gs = rand(vec2(texCoord0.x + time, texCoord0.y));
        newColor = mix(newColor, vec3(gs, gs, gs), grainIntensity);
    }

    fragColor = vec4(newColor, color.a) * ColorModulator;
}
