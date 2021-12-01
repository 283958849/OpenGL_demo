#version 300 es
precision mediump float;
uniform sampler2D textureUnit;
in vec2 vTextCoord;
out vec4 vFragColor;
void main() {
    vFragColor = texture(textureUnit, vTextCoord);
}