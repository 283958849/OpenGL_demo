package com.example.opengltest;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Activity class for example program that detects OpenGL ES 2.0.
 **/
public class MainActivity extends Activity {

    private final static String TAG = "OpenGLES TEST";

    private GLSurfaceView mGLSurfaceView = null;
    private MyRenderer mRenderer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        if (!detectOpenGLES30()) {
            Toast.makeText(this, "GLES 3.0 not supported!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mRenderer = new MyRenderer(this);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(mGLSurfaceView);
    }

    private boolean detectOpenGLES30()
    {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        Log.e(TAG, "opengles version:" + info.reqGlEsVersion);
        return (info.reqGlEsVersion >= 0x30000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }
}
