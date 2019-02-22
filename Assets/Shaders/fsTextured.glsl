#version 330

in vec4 fColor;
in vec2 fTexCoord;

out vec4 fragColor;

uniform sampler2D tex;

void main() {
    fragColor = texture(tex, fTexCoord) * fColor;
}
