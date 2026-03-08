#version 150

in vec4 vertexColor;

uniform float startTrueAnomaly;
uniform float endTrueAnomaly;
uniform vec4 ColorModulator;

in vec3 vertPos;
out vec4 fragColor;

#define PI 3.141592653589
#define h_PI 1.57079632679

float adjustedTrueAnomaly(vec3 vertPos) {
    float currentAnamoly = atan(vertPos.z, vertPos.x - 2);
    return currentAnamoly;
}

void main() {
    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }

    float isVisble = 1.0;
    float anomaly = adjustedTrueAnomaly(vertPos);

    if (startTrueAnomaly >= anomaly) {
        isVisble = 0.0;
    }

    //isVisble = anomaly / (2 * PI);

    fragColor = color * ColorModulator * isVisble;
    // fragColor = vec4(isVisble, 0.0, 0.0, 1.0);
}