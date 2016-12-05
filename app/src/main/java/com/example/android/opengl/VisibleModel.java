package com.example.android.opengl;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.momchil_atanasov.data.front.parser.OBJDataReference;
import com.momchil_atanasov.data.front.parser.OBJFace;
import com.momchil_atanasov.data.front.parser.OBJMesh;
import com.momchil_atanasov.data.front.parser.OBJModel;
import com.momchil_atanasov.data.front.parser.OBJNormal;
import com.momchil_atanasov.data.front.parser.OBJObject;
import com.momchil_atanasov.data.front.parser.OBJVertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CaiHaozhong on 2016/11/29.
 */
public class VisibleModel {

    private static final String TAG = "VisibleModel";
    private OBJModel mRawData = null;

    private BoundingBox mBoundingBox = null;

    private final String vertexShaderCode =
            "uniform vec3 u_LightPos;\n" +
            "uniform mat4 u_ProjectMatrix;\n" +
            "uniform mat4 u_EyeMatrix;\n" +
            "uniform mat4 u_RotateMatrix;\n" +
            "uniform mat4 u_AccumulateRotateMatrix;\n" +
            "uniform mat4 u_TranCenterMatrix;\n" +
            "attribute vec4 a_Position;\n" +
            "attribute vec3 a_Normal;\n" +
            "varying vec4 v_Color;\n" +
            "void main()\n" +
            "{\n" +
            "mat4 u_MVMatrix = u_EyeMatrix * u_RotateMatrix * u_AccumulateRotateMatrix * u_TranCenterMatrix;\n" +
            "mat4 u_MVPMatrix = u_ProjectMatrix * u_MVMatrix;\n" +
            "vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n" +
            "vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n" +
            "float distance = length(u_LightPos - modelViewVertex);             \n" +
            "vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n" +
            "float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n" +
//            "diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));  \n" +
            "v_Color = vec4(1,1,1,0) * diffuse;                         \n" +
            "gl_Position = u_MVPMatrix * a_Position;                            \n" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;\n" +
            "varying vec4 v_Color;\n" +
            "void main()\n" +
            "{\n" +
            "   gl_FragColor = v_Color;\n" +
            "}";

    private FloatBuffer vertexBuffer;
    private IntBuffer drawListBuffer;
    private FloatBuffer normalBuffer;
    private int mProgram;
    private int mPositionHandle = 1;
    private int mNormalHandle = 2;
    private int mColorHandle;
    private float[] mTranCenterMatrix = new float[16];
    private float[] mAccumulateRotateMatrix = new float[16];
    private float[] mProjectMatrix = null;
    private float[] mEyeMatrix = null;
    private float[] mLightPos = null;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private ArrayList<Integer> drawOrder;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public VisibleModel(OBJModel rawData) {
        setRawData(rawData);
    }

    public VisibleModel(){}

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     * @param rotateMatrix - Use for trackball, no matter the center of the object is (0,0,0) or not.
     * this shape.
     */
    public void draw(float[] rotateMatrix) {

        if(mRawData == null)
            return;
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        /** Ugly code begin **/
        if(GLES20.glGetError() != GLES20.GL_NO_ERROR){
            init();
        }
        /** Ugly code end **/

        GLES20.glUseProgram(mProgram);
        MyGLRenderer.checkGlError("glUseProgram");

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        MyGLRenderer.checkGlError("glEnableVertexAttribArray");
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, normalBuffer);

        // get handle to shape's transformation matrix
        int lightHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");
        int projMHandle = GLES20.glGetUniformLocation(mProgram, "u_ProjectMatrix");
        int eyeMHandle = GLES20.glGetUniformLocation(mProgram, "u_EyeMatrix");
        int rotateMHandle = GLES20.glGetUniformLocation(mProgram, "u_RotateMatrix");
        int accumuRotateMHandle = GLES20.glGetUniformLocation(mProgram, "u_AccumulateRotateMatrix");
        int transCenterMHandle = GLES20.glGetUniformLocation(mProgram, "u_TranCenterMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the transformation
        GLES20.glUniform3fv(lightHandle, 1, mLightPos, 0);
        GLES20.glUniformMatrix4fv(projMHandle, 1, false, mProjectMatrix, 0);
        GLES20.glUniformMatrix4fv(eyeMHandle, 1, false, mEyeMatrix, 0);
        GLES20.glUniformMatrix4fv(rotateMHandle, 1, false, rotateMatrix, 0);
        GLES20.glUniformMatrix4fv(accumuRotateMHandle, 1, false, mAccumulateRotateMatrix, 0);
        GLES20.glUniformMatrix4fv(transCenterMHandle, 1, false, mTranCenterMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");



        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.size(),
                GLES20.GL_UNSIGNED_INT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
    }

    public void setRawData(OBJModel rawData){
        mRawData = rawData;
        if(mRawData != null)
            init();
    }

    private void init(){
        List<OBJVertex> vertices = mRawData.getVertices();
        List<OBJNormal> normals = mRawData.getNormals();
        mBoundingBox = computeBoundingBox(vertices);

        Matrix.setIdentityM(mTranCenterMatrix, 0);
        Matrix.setIdentityM(mAccumulateRotateMatrix, 0);
        Matrix.translateM(mTranCenterMatrix, 0, -mBoundingBox.center.x(), -mBoundingBox.center.y(), -mBoundingBox.center.z());

        mLightPos = new float[]{(mBoundingBox.maxPoint.x()+mBoundingBox.minPoint.x())/2, (mBoundingBox.minPoint.y()+mBoundingBox.maxPoint.y())/2, mBoundingBox.maxPoint.z()*2};

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                vertices.size() * 3 * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        for(OBJVertex v : vertices){
            vertexBuffer.put(v.x);
            vertexBuffer.put(v.y);
            vertexBuffer.put(v.z);
        }
        vertexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(normals.size() * 3 * 4);
        bb.order(ByteOrder.nativeOrder());
        normalBuffer = bb.asFloatBuffer();
        for(OBJNormal n : normals){
            normalBuffer.put(n.x);
            normalBuffer.put(n.y);
            normalBuffer.put(n.z);
        }
        normalBuffer.position(0);

        drawOrder = new ArrayList<Integer>();
        drawOrder.ensureCapacity(vertices.size() * 3);
        List<OBJObject> objObjects = mRawData.getObjects();
        Log.v(TAG, "objects: " + objObjects.size());
        for(OBJObject o : objObjects){
            List<OBJMesh> meshs = o.getMeshes();
            for(OBJMesh m : meshs){
                List<OBJFace> faces = m.getFaces();
                for(OBJFace f : faces) {
                    List<OBJDataReference> indices = f.getReferences();
                    for (OBJDataReference i : indices){
                        drawOrder.add(i.vertexIndex);
                    }
                }
            }
        }
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.size() * 4);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asIntBuffer();
        for(Integer index : drawOrder) {
            drawListBuffer.put(index);
        }
        drawListBuffer.position(0);


        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program

        GLES20.glBindAttribLocation(mProgram, mPositionHandle, "a_Position");
        GLES20.glBindAttribLocation(mProgram, mNormalHandle, "a_Normal");
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }
    private BoundingBox computeBoundingBox(List<OBJVertex> vertices){
        Vec3f minP = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE), maxP = new Vec3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for(OBJVertex v : vertices){
            Vec3f temp = new Vec3f(v.x, v.y, v.z);
            minP.makeMin(temp);
            maxP.makeMax(temp);
        }
        return new BoundingBox(minP, maxP);
    }

    public BoundingBox getBoundingBox(){
        return mBoundingBox;
    }

    public void setProjectMatrix(float[] projectMatrix){
        this.mProjectMatrix = projectMatrix;
    }

    public void setEyeMatrix(float[] eyeMatrix){
        this.mEyeMatrix = eyeMatrix;
    }

    public void accumulateRotate(float[] rotateMatrix){
        float[] lastRotate = new float[16];
        System.arraycopy(mAccumulateRotateMatrix, 0, lastRotate, 0, 16);
        Matrix.multiplyMM(mAccumulateRotateMatrix, 0, rotateMatrix, 0, lastRotate, 0);
    }
}
