package com.example.android.opengl;

/**
 * Created by CaiHaozhong on 2016/11/29.
 */

public class Vec3f {
//    public float x;
//
//    public float y;
//
//    public float z;

    public static final float UNDEFINE = Float.MIN_VALUE;

    private float[] mRaw = null;

    public Vec3f(){}

    public Vec3f(float x, float y, float z){
        mRaw = new float[3];
        set(x,y,z);
    }

    public float x(){
        if(mRaw == null)
            return UNDEFINE;
        return mRaw[0];
    }

    public float y(){
        if(mRaw == null)
            return UNDEFINE;
        return mRaw[1];
    };

    public float z(){
        if(mRaw == null)
            return UNDEFINE;
        return mRaw[2];
    }

    public void set(float x, float y, float z){
        mRaw[0] = x;
        mRaw[1] = y;
        mRaw[2] = z;
    }

    public float[] getRawData(){
        return mRaw;
    }

    public void makeMin(Vec3f other){
        float[] otherRaw = other.getRawData();
        for(int i = 0; i < 3; i++){
            if(mRaw[i] > otherRaw[i])
                mRaw[i] = otherRaw[i];
        }
    }

    public void makeMax(Vec3f other){
        float[] otherRaw = other.getRawData();
        for(int i = 0; i < 3; i++){
            if(mRaw[i] < otherRaw[i])
                mRaw[i] = otherRaw[i];
        }
    }

    public Vec3f add(Vec3f other){
        return new Vec3f(mRaw[0] + other.x(), mRaw[1] + other.y(), mRaw[2] + other.z());
    }

    public Vec3f divByScalar(float d){
        return new Vec3f(mRaw[0]/d, mRaw[1]/d, mRaw[2]/d);
    }

    public Vec3f sub(Vec3f other){
        return new Vec3f(mRaw[0] - other.x(), mRaw[1] - other.y(), mRaw[2] - other.z());
    }

    public float length(){
        return (float)Math.sqrt(sqrLength());
    }

    /** 模的平方 **/
    public float sqrLength(){
        return mRaw[0]*mRaw[0] + mRaw[1]*mRaw[1] + mRaw[2]*mRaw[2];
    }

    /** 单位化 **/
    public Vec3f nomalize() {
        float len = length();
        return new Vec3f(mRaw[0]/len, mRaw[1]/len, mRaw[2]/len);
    }

    public float dotProduct(Vec3f other){
        return mRaw[0]*other.x() + mRaw[1]*other.y() + mRaw[2]*other.z();
    }

    public Vec3f crossProduct(Vec3f other){
        float x,y,z;
        x = mRaw[1]*other.z() - mRaw[2]*other.y();
        y = mRaw[2]*other.x() - mRaw[0]*other.z();
        z = mRaw[0]*other.y() - mRaw[1]*other.x();
        return new Vec3f(x, y, z);
    }

}
