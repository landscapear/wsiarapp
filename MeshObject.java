/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.vuforia.samples.FloodAR;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class MeshObject
{
    public float angle_degrees_x, angle_degrees_y, angle_degrees_z;
    public float scale_x, scale_y, scale_z;
    public float trans_x, trans_y, trans_z;

    public double [] v1, v2;
	public double bot = 60;		// these values should always be positive
	public double top = 0;
	public double back = 300;
	public double front = 0;
	public double archwidth = 0;
	public double bridgetopheight = 10;
	public double archeight = 10;
	public double degrees = 0;	// swing angle from the pinch corner (in degrees)

    public MeshObject()
    {
		this.angle_degrees_x = 0;
		this.angle_degrees_y = 0;
		this.angle_degrees_z = 0;
		this.scale_x = 1;
		this.scale_y = 1;
		this.scale_z = 1;
		this.trans_x = 0;
		this.trans_y = 0;
		this.trans_z = 0;
    }
    
    public MeshObject(MeshObject c)
    {
		this.angle_degrees_x = c.angle_degrees_x;
		this.angle_degrees_y = c.angle_degrees_y;
		this.angle_degrees_z = c.angle_degrees_z;
		this.scale_x = c.scale_x;
		this.scale_y = c.scale_y;
		this.scale_z = c.scale_z;
		this.trans_x = c.trans_x;
		this.trans_y = c.trans_y;
		this.trans_z = c.trans_z;
    }
    
    public MeshObject(float tx, float ty, float tz, float rx, float ry, float rz, float sx, float sy, float sz)
    {    	
		this.angle_degrees_x = rx;
		this.angle_degrees_y = ry;
		this.angle_degrees_z = rz;
		this.scale_x = sx;
		this.scale_y = sy;
		this.scale_z = sz;
		this.trans_x = tx;
		this.trans_y = ty;
		this.trans_z = tz;
    }
   
    public enum BUFFER_TYPE
    {
        BUFFER_TYPE_VERTEX, BUFFER_TYPE_TEXTURE_COORD, BUFFER_TYPE_NORMALS, BUFFER_TYPE_INDICES
    }
    
    
    public Buffer getVertices()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_VERTEX);
    }
    
    
    public Buffer getTexCoords()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_TEXTURE_COORD);
    }
    
    
    public Buffer getNormals()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_NORMALS);
    }
    
    
    public Buffer getIndices()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_INDICES);
    }
    
    
    protected Buffer fillBuffer(double[] array)
    {
        // Convert to floats because OpenGL doesn't work on doubles, and manually
        // casting each input value would take too much time.
        // Each float takes 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (double d : array)
            bb.putFloat((float) d);
        bb.rewind();
        
        return bb;
        
    }
    
    
    protected Buffer fillBuffer(float[] array)
    {
        // Each float takes 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (float d : array)
            bb.putFloat(d);
        bb.rewind();
        
        return bb;
        
    }
    
    
    protected Buffer fillBuffer(short[] array)
    {
        // Each short takes 2 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(2 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : array)
            bb.putShort(s);
        bb.rewind();
        
        return bb;
        
    }
    
    
    public abstract Buffer getBuffer(BUFFER_TYPE bufferType);
    
    
    public abstract int getNumObjectVertex();
    
    
    public abstract int getNumObjectIndex();

    public MeshObject getInstance()
    {
    	return this;
    }
}
