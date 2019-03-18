#version 330 core

in vec4 fColor;
in vec2 fTexCoord;
in vec3 fmvVertNormal;
in vec3 fmvVertPos;

out vec4 fragColor;

struct Attenuation {
    float constant;
    float linear;
    float exponent;
};

struct PointLight {
    vec3 color;
    vec3 position;
    float intensity;
    Attenuation att;
};

struct Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    float reflectance;
};

uniform sampler2D texture_sampler;
uniform vec4 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PointLight pointLight;

vec4 ambientC;
vec4 diffuseC;
vec4 specularC;

void setupColors(Material material, vec2 texCoord) {
    if (material.hasTexture == 1) {
        ambientC = texture(texture_sampler, texCoord);
        diffuseC = ambientC;
        specularC = ambientC;
    } else {
        ambientC = material.ambient;
        diffuseC = material.diffuse;
        specularC = material.specular;
    }
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal) {
    vec4 diffuseColor = vec4(0, 0, 0, 0);
    vec4 specColor = vec4(0, 0, 0, 0);

    vec3 light_direction = light.position - position;
    vec3 to_light_source  = normalize(light_direction);
    float diffuseFactor = max(dot(normal, to_light_source), 0.0);
    diffuseColor = diffuseC * vec4(light.color, 1.0) * light.intensity * diffuseFactor;

    vec3 camera_direction = normalize(-position);
    vec3 from_light_source = -to_light_source;
    vec3 reflected_light = normalize(reflect(from_light_source, normal));
    float specularFactor = max(dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColor = specularC * specularFactor * material.reflectance * vec4(light.color, 1.0);

    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
    light.att.exponent * distance * distance;
    return (diffuseColor + specColor) / attenuationInv;
}

void main() {
    setupColors(material, fTexCoord);

    vec4 diffuseSpecularComp = calcPointLight(pointLight, fmvVertPos, fmvVertNormal);

    fragColor = ambientC * ambientLight + diffuseSpecularComp;
}
