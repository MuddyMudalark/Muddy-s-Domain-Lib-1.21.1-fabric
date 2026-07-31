#version 150

#moj_import <projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord;
out vec3 vertexNormal;
uniform vec3 ChunkOffset;
uniform vec3 CameraPosition;
out vec3 viewPosition;
out vec3 worldPosition;
out vec4 texProj0;

void main() {
    vec3 pos = Position + ChunkOffset;

    worldPosition = CameraPosition - pos;

    vec4 transformedPosition = ModelViewMat * vec4(pos, 1.0);

    gl_Position = ProjMat * transformedPosition;

    vertexColor = Color;
    texCoord = UV0;
    vertexNormal = Normal;
    viewPosition = transformedPosition.xyz;

    texProj0 = projection_from_position(gl_Position);
}