#version 150

in vec4 vertexColor;

uniform vec4 nspOverlayColor;
uniform vec4 nspAtmoColor;
uniform float nspOverlayAngle;
uniform float nspAtmoAngle;

out vec4 fragColor;
in vec3 vertPos;

void main() {
    vec4 color = vertexColor;
    vec3 normalizedVector = normalize(vertPos);
    float normalY =  normalizedVector.y;

    float sphereOutlineAngle = (normalY - nspOverlayAngle)/(1-nspOverlayAngle);
    vec4 planetOverlayColor = mix(nspAtmoColor, nspOverlayColor, clamp(sphereOutlineAngle, 0.0, 1.0)) * step(nspOverlayAngle,  normalY);
    float atmoPercent = (normalY - nspOverlayAngle)/(nspAtmoAngle - nspOverlayAngle);

    //alpha scales with negative of (x-1)^2
    float atmoshpereAlpha = clamp((atmoPercent-1)*(atmoPercent-1), 0.0, 1.0) * step(nspAtmoAngle, normalY);

    fragColor = planetOverlayColor + vec4(nspAtmoColor.r, nspAtmoColor.g, nspAtmoColor.b, atmoshpereAlpha) ;
}
