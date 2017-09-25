#version 330

uniform sampler2D uCloud0;
uniform sampler2D uCloud1;

uniform vec2 uCloud0Offset;
uniform vec2 uCloud1Offset;

in vec2 vTexCoord;
in float vY;

out vec4 outColor;

void main() {
    vec3 colorLow  = vec3(0.516, 0.986, 1.000);
    vec3 colorHigh = vec3(0.133, 0.353, 0.725);
    vec3 color = mix(colorLow, colorHigh, vY);

    vec3 texCloud0 = texture(uCloud0, vTexCoord * 3.0 + uCloud0Offset).xyz;
    vec3 texCloud1 = texture(uCloud1, vTexCoord * 3.0 + uCloud1Offset).xyz;

    color = clamp(color + texCloud0 + texCloud1, 0.0, 1.0);

    outColor = vec4(color, 1.0);
}