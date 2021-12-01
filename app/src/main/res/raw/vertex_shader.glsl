#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec2 aTextCoord;
uniform mat4 uMatrix;
out vec2 vTextCoord;
void main() {
    gl_Position  = uMatrix * vPosition;
    gl_PointSize = 20.0;
    vTextCoord = aTextCoord;
}