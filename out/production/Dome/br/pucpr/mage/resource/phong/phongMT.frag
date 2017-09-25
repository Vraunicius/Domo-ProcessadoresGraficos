#version 330

uniform vec3 uLightDir;

uniform vec3 uAmbientLight;
uniform vec3 uDiffuseLight;
uniform vec3 uSpecularLight;

uniform vec3 uAmbientMaterial;
uniform vec3 uDiffuseMaterial;
uniform vec3 uSpecularMaterial;

uniform float uSpecularPower;

uniform sampler2D uTexture1;
uniform sampler2D uTexture2;
uniform sampler2D uTexture3;
uniform sampler2D uTexture4;

in vec3 vNormal;
in vec3 vViewPath;
in vec2 vTexCoord;

in float vDepth;
in vec4 vTexWeight;

out vec4 outColor;

void main() {
    vec3 L = normalize(uLightDir);
	vec3 N = normalize(vNormal);

    vec3 ambient = uAmbientLight * uAmbientMaterial;
    
    float diffuseIntensity = max(dot(N, -L), 0.0);
    vec3 diffuse = diffuseIntensity * uDiffuseLight * uDiffuseMaterial;
       
    //Calculo do componente especular
	float specularIntensity = 0.0;
	if (uSpecularPower > 0.0) {
		vec3 V = normalize(vViewPath);
		vec3 R = reflect(L, N);
		specularIntensity = pow(max(dot(R, V), 0.0), uSpecularPower);
	}
    vec3 specular = specularIntensity * uSpecularLight * uSpecularMaterial;
    
    //Calculo das texturas
    float blendDistance = 0.99;
    float blendWidth = 100;
    float blendFactor = clamp(
    	(vDepth - blendDistance) * blendWidth, 0.0, 1.0); 
       
    vec2 nearCoord = vTexCoord * 50.0;
    vec4 texelNear = 
    	texture(uTexture1, nearCoord) * vTexWeight.w +
    	texture(uTexture2, nearCoord) * vTexWeight.z +
    	texture(uTexture3, nearCoord) * vTexWeight.y +
        texture(uTexture4, nearCoord) * vTexWeight.x;
     
    vec2 farCoord = vTexCoord * 10.0;
    vec4 texelFar = 
    	texture(uTexture1, farCoord) * vTexWeight.w +
    	texture(uTexture2, farCoord) * vTexWeight.z +
    	texture(uTexture3, farCoord) * vTexWeight.y +
        texture(uTexture4, farCoord) * vTexWeight.x;

    vec4 texel = mix(texelNear, texelFar, blendFactor);
    
    vec3 color = clamp(texel.rgb * (ambient + diffuse) + specular, 0.0, 1.0);
    outColor = vec4(color, texel.a);
}