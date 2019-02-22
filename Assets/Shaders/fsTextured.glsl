#version 330

in vec4 fColor;
in vec2 fTexCoord;

uniform sampler2D tex;

out vec4 fragColor;

void main() {
    fragColor = texture(tex, fTexCoord) * fColor;
}
