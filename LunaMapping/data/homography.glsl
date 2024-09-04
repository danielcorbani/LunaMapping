#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PROCESSING_TEXTURE_SHADER

uniform sampler2D texture;
uniform vec2 resolution;
uniform mat3 H;
uniform vec2 xy1;
uniform vec2 xy2;
uniform vec2 xy3;
uniform vec2 xy0;


void main(void) {
  vec2 p;
  p.x = gl_FragCoord.x/ resolution.x;
  p.y = gl_FragCoord.y/ resolution.y;
  
  vec3 pp = vec3(p,1.0);
  vec3 uvk = H*pp;
  vec2 ab = uvk.xy/uvk.z;

  vec3 xyz1 = vec3(xy1,1.0);
  vec3 uv1 = H*xyz1;
  vec2 ab1 = uv1.xy/uv1.z;

  vec3 xyz2 = vec3(xy2,1.0);
  vec3 uv2 = H*xyz2;
  vec2 ab2 = uv2.xy/uv2.z;

  vec3 xyz3 = vec3(xy3,1.0);
  vec3 uv3 = H*xyz3;
  vec2 ab3 = uv3.xy/uv3.z;

  vec3 xyz0 = vec3(xy0,1.0);
  vec3 uv0 = H*xyz0;
  vec2 ab0 = uv0.xy/uv0.z;
  
  float angle = acos( dot( normalize(ab0.xy-ab.xy), normalize(ab1.xy-ab.xy) ) );
  angle += acos( dot( normalize(ab1.xy-ab.xy), normalize(ab2.xy-ab.xy) ) );
  angle += acos( dot( normalize(ab2.xy-ab.xy), normalize(ab3.xy-ab.xy) ) );
  angle += acos( dot( normalize(ab3.xy-ab.xy), normalize(ab0.xy-ab.xy) ) );

  //maps texture using ab coordinates; xyz are rgb colors
  vec3 col = texture2D(texture, ab).xyz;
  
  //if (ab.x>=0.0 && ab.x<=1.0 && ab.y>=0.0 && ab.y<=1.0 ) {
  if (angle > 6.28 ) {
       gl_FragColor = vec4(col, 1.0);
   } else {
       gl_FragColor = vec4(0.0,0.0,0.0,0.0);
   }
}