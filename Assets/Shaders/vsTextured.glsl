#version 330 core

layout (location=0) in vec4 vPos;
layout (location=1) in vec4 vColor;
layout (location=2) in vec2 vTexCoord;

out vec4 fColor;
out vec2 fTexCoord;

uniform mat4 worldMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * worldMatrix * vPos;
    fColor = vColor;
    fTexCoord = vTexCoord;
}