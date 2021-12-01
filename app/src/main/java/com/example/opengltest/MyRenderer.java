package com.example.opengltest;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "MyRenderer";

    private Context context = null;

    /**
     * 顶点坐标
     * (x,y,z)
     */
    private static final float[] POSITION_VERTEX = new float[]{
            0f, 0f, 0f,     //顶点坐标V0
            1f, 1f, 0f,     //顶点坐标V1
            -1f, 1f, 0f,    //顶点坐标V2
            -1f, -1f, 0f,   //顶点坐标V3
            1f, -1f, 0f     //顶点坐标V4
    };

    /**
     * 纹理坐标
     * (s,t)
     */
    private static final float[] TEX_VERTEX = {
            0.5f, 0.5f, //纹理坐标V0
            1f, 0f,     //纹理坐标V1
            0f, 0f,     //纹理坐标V2
            0f, 1.0f,   //纹理坐标V3
            1f, 1.0f    //纹理坐标V4
    };

    /**
     * 绘制顺序索引
     */
    private static final short[] VERTEX_INDEX = {
            0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
            0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
            0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
            0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
    };

    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private int mMatrixLocation;
    private int mPositionLocation;
    private int mTextureLocation;

    private FloatBuffer mVertexBuffer = null;
    private FloatBuffer mTextVertexBuffer = null;
    private ShortBuffer mVertexIndexBuffer = null;
    private int mProgram = -1;

    private int mTextureId = -1;

    private Bitmap mBitmap = null;


    public MyRenderer(Context context) {
        this.context = context;

        mVertexBuffer = ByteBuffer.allocateDirect(POSITION_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(POSITION_VERTEX);
        mVertexBuffer.position(0);


        mTextVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_VERTEX);
        mTextVertexBuffer.position(0);

        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX);
        mVertexIndexBuffer.position(0);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        final int[] compiledStatus = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiledStatus, 0);
        if (compiledStatus[0] == 0) {
            Log.e(TAG, GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, readResource(R.raw.vertex_shader));
        int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, readResource(R.raw.fragment_shader));

        mProgram = GLES30.glCreateProgram();
        if (mProgram == 0) {
            Log.e(TAG, "GLES30.glCreateProgram failed!");
            return;
        }

        GLES30.glAttachShader(mProgram, vertexShader);
        GLES30.glAttachShader(mProgram, fragmentShader);
        GLES30.glLinkProgram(mProgram);

        final int[] status = new int[1];
        GLES30.glGetShaderiv(mProgram, GLES30.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, GLES30.glGetShaderInfoLog(mProgram));
            GLES30.glDeleteShader(mProgram);
        }

        GLES30.glUseProgram(mProgram);

        mMatrixLocation = GLES30.glGetUniformLocation(mProgram, "uMatrix");
        mPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition");
        mTextureLocation = GLES30.glGetAttribLocation(mProgram, "aTextCoord");

        mTextureId = loadTexture(context, R.drawable.dir);
    }

    private int loadTexture(Context context, int resourceId) {
        final int[] textureIds = new int[1];
        //创建一个纹理对象
        GLES30.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL textureId object.");
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        //这里需要加载原图未经缩放的数据
        options.inScaled = false;
        mBitmap= BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (mBitmap == null) {
            Log.e(TAG, "Resource ID " + resourceId + " could not be decoded.");
            GLES30.glDeleteTextures(1, textureIds, 0);
            return 0;
        }

        //绑定纹理到OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]);

        //设置默认的纹理过滤参数
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        //加载bitmap到纹理中
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mBitmap, 0);

        //生成MIP贴图
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

        //数据如果已经被加载进OpenGL,则可以回收该bitmap
        mBitmap.recycle();

        //取消绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        return textureIds[0];
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);

        int w=mBitmap.getWidth();
        int h=mBitmap.getHeight();
        float sWH=w/(float)h;
        float sWidthHeight=width/(float)height;
        if(width>height){
            if(sWH>sWidthHeight){
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight*sWH,sWidthHeight*sWH, -1,1, 3, 7);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight/sWH,sWidthHeight/sWH, -1,1, 3, 7);
            }
        }else{
            if(sWH>sWidthHeight){
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1/sWidthHeight*sWH, 1/sWidthHeight*sWH,3, 7);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH/sWidthHeight, sWH/sWidthHeight,3, 7);
            }
        }

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        // transform matrix to vertex shader
        GLES30.glUniformMatrix4fv(mMatrixLocation, 1, false, mMVPMatrix, 0);

        // use vertex coordinate
        GLES30.glEnableVertexAttribArray(mPositionLocation);
        GLES30.glVertexAttribPointer(mPositionLocation, 3, GLES30.GL_FLOAT, false, 0, mVertexBuffer);

        // use texture coordinate
        GLES30.glEnableVertexAttribArray(mTextureLocation);
        GLES30.glVertexAttribPointer(mTextureLocation, 2, GLES30.GL_FLOAT, false, 0, mTextVertexBuffer);

        // activate texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

        // bind texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);

        //3 points
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, VERTEX_INDEX.length, GLES30.GL_UNSIGNED_SHORT, mVertexIndexBuffer);

        // disable vertex array handle
        GLES30.glDisableVertexAttribArray(mPositionLocation);
        GLES30.glDisableVertexAttribArray(mTextureLocation);
    }

    private String readResource(int resourceId) {
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader streamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String textLine;
            while ((textLine = bufferedReader.readLine()) != null) {
                builder.append(textLine);
                builder.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }
}


