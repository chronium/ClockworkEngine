#version 330 core

layout (location=0) in vec4 vPos;
layout (location=1) in vec2 vTexCoord;
layout (location=2) in vec3 vNormal;

out vec2 fTexCoord;
out vec3 fmvVertNormal;
out vec3 fmvVertPos;

uniform mat4 worldMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
    vec4 mvPos = viewMatrix * worldMatrix * vPos;
    gl_Position = projectionMatrix * mvPos;
    fTexCoord = vTexCoord;
    fmvVertNormal = normalize(viewMatrix * worldMatrix * vec4(vNormal, 0.0)).xyz;
    fmvVertPos = mvPos.xyz;
}
