/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;

import com.caihaozhong.importer.ImporterCallBack;
import com.caihaozhong.importer.LocalFileImporter;
import com.momchil_atanasov.data.front.parser.OBJFace;
import com.momchil_atanasov.data.front.parser.OBJModel;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView implements ImporterCallBack{

    private static final String TAG = "MyGLSurfaceView";

    private final MyGLRenderer mRenderer;

    private LocalFileImporter mFileImporter;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;

    private float mPreviousGLX;
    private float mPreviousGLY;
    private float mPreviousGLZ;

    /** Rotation Axis and Angle **/
    private Vec3f mRotationAXIS;
    private float mRotationAngle;

    private float mTrackBallRaidusSqare;
    private float mWindowWidth, mWindowHeight;
    private float mWindowRadio;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Import ObjModel
        mFileImporter = new LocalFileImporter();
        mFileImporter.setFileName("/storage/emulated/0/combine/mesh000.obj");//MaleUpper03closedResize
        mFileImporter.addImporterCallBack(this);
        mFileImporter.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float curGLX = x - mWindowWidth/2;
                float curGLY = (mWindowHeight/2 - y) * mWindowRadio;

                /** Prevent NaN axix **/
                if(Math.abs(curGLX-mPreviousGLX) < 2f || Math.abs(curGLY-mPreviousGLY) < 2f)
                    break;
                computeRotation(curGLX, curGLY);
                Log.v(TAG, "axis: " + mRotationAXIS.x() + " " + mRotationAXIS.y() + " " + mRotationAXIS.z());
                Log.v(TAG, "angle: " + mRotationAngle);
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.setRotation(mRotationAXIS, mRotationAngle);
                        MyGLSurfaceView.this.requestRender();
                    }
                });
                break;
            case MotionEvent.ACTION_DOWN:
                mPreviousGLX = x - mWindowWidth/2;
                mPreviousGLY = (mWindowHeight/2 - y) * mWindowRadio;
                mPreviousGLZ = (float)Math.sqrt(mTrackBallRaidusSqare - mPreviousGLX*mPreviousGLX - mPreviousGLY*mPreviousGLY);
                break;
            case MotionEvent.ACTION_UP:
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.storeRotation();
                    }
                });

                break;
        }
        return true;
    }

    @Override
    public void onImportBegin() {
        Log.v(TAG, "Begin import!");
//        Log.v(TAG,Environment.getExternalStorageDirectory().getPath());
    }

    @Override
    public void onImportEnd() {
        Log.v(TAG, "End import!");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.setVisibleModel(new VisibleModel(mFileImporter.getModel()));
                MyGLSurfaceView.this.requestRender();
            }
        });
    }

    private void computeRotation(float curGLX, float curGLY){
        float curGLZ = (float)Math.sqrt(mTrackBallRaidusSqare - curGLX*curGLX - curGLY*curGLY);
        Vec3f v1 = new Vec3f(mPreviousGLX, mPreviousGLY, mPreviousGLZ);
        Vec3f v2 = new Vec3f(curGLX, curGLY, curGLZ);
        mRotationAngle = (float)(Math.acos(v1.dotProduct(v2) / Math.sqrt(v1.sqrLength()*v2.sqrLength())) * 180 / Math.PI);
        mRotationAXIS = v1.crossProduct(v2).nomalize();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
        mWindowWidth = r - l;
        mWindowHeight = b - t;
        float tmp = Math.max(mWindowHeight, mWindowWidth)/2;
        mTrackBallRaidusSqare = tmp * tmp;
        mWindowRadio = mWindowWidth / mWindowHeight;
        Log.v(TAG, "radius: " + Math.sqrt(mTrackBallRaidusSqare));
    }
}
