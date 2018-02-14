/*===============================================================================
FloodAppRenderer code:

Copyright (c) 2016 Dep. Landscape, University of Sheffield.



Based on the UserDefinedTargets Sample App Renderer:

Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.FloodAR;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleAppRenderer;
import com.vuforia.samples.SampleApplication.SampleAppRendererControl;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.CubeObject;
import com.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.vuforia.samples.SampleApplication.utils.SampleMath;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.vuforia.samples.SampleApplication.utils.Texture;

// The renderer class for the ImageTargetsBuilder sample.
public class FloodAppRenderer implements GLSurfaceView.Renderer, SampleAppRendererControl {
    public enum Mode {
        NONE, TRIANGULATEPOINTS, DELETEPOINTS, ADDOCCLUSIONGEOMETRY, ADDOCCLUSIONGEOMETRY2,
        STRETCH_TOP_SURFACE, STRECTH_BOTTOM_SURFACE, STRETCH_BACK_SURFACE, STRETCH_FRONT_SURFACE, CHANGE_ARCH_WIDTH,
        SELECTOCCLUSIONGEOMETRY, SCALING,
        SELECTFIRSTPOINT, SELECTSECONDPOINT,
        DEFINEFLOODPLANE,
        SELECTANNOTATIONPOINT, ANNOTATIONPOINTSELECTED,
        SELECTANNOTATIONTODISPLAY
    }
    public enum Palette {RED, GREEN, BLUE, WHITE}
    private static final String LOGTAG = "Renderer";
    static double floodHeight = 0;
    static double minFloodHeight = -200;
    static double maxFloodHeight = 200;
    static double minFloodHeightMeters = 0.0;
    static double maxFloodHeightMeters = 1.0;
    static double floodPlaneWidth = 100;
    static double floodPlaneDepth = 100;
    static double floodPlaneXOffset = 0;
    static double floodPlaneZOffset = 0;
    static double floodAngle = 0;
    static double floodPadding = 100f;
    volatile static boolean addline = false;
    volatile static boolean selectDelete = false;
    public Mode primaryMode = Mode.NONE;
    public Mode secondaryMode = Mode.NONE;
    public static boolean occGeomVisible = true;
    public static boolean showFlood = false;
    private int occGeomType = 0;
    private final CharSequence[] items = {"Block", "Arch"};
    int annotIndex;
    int angle = 0;
    int bipoint_counter = 0;
    private int vert = -1;
    private int vertold = -1;
    private int vertfirst = -1;
    private int selectedOcclusionGeometry = -1;
    private int first_vert = -1;
    private int second_vert = -1;
    private int colorHandle;
    Triangulator mymath = new Triangulator();
    String annotTitleString, annotEditString;
    Toast floodToast;
    String annotationText, annotationSensor;
    Annotation annot = null;
    EditText txtedit;
    private PlaneObject mPlane1;
    private MeshObject mPlane2;
    private MeshObject mSphere;
    private MeshObject mCube;
    static ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    static ArrayList<double[]> vertices;
    static ArrayList<MeshObject> occgeom = new ArrayList<MeshObject>();
    ArrayList<double[]> line_vertices_array;
    ArrayList<ArrayList<double[]>> data = new ArrayList<ArrayList<double[]>>();
    double[] result = new double[3];
    ByteBuffer line_vertices_buffer;
    ByteBuffer line_colours_buffer;
    ByteBuffer triangle_vertices_buffer;
    public String pointDataStr = "";

    //

    private float[] modelViewMatrix = new float[16];
    private float[] modelViewMatrixInverse = new float[16];
    private Matrix44F modelViewMatrix44F;
    private Matrix44F modelViewMatrixInverse44F;
    private Matrix44F modelViewMatrixInverseTranspose44F;
    private Matrix44F projectionMatrix44F = new Matrix44F();
    private Matrix44F projectionMatrixInverse44F;
    private Matrix44F projectionMatrixInverseTranspose44F;
    private SampleApplicationSession vuforiaAppSession;
    private SampleAppRenderer mSampleAppRenderer;
    private boolean mIsActive = false;
    private Vector<Texture> mTextures;
    private int shaderProgramID;
    private int normalHandle;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;
    static final float kObjectScale = 3.f;
    private FloodApp mActivity;

    public FloodAppRenderer(FloodApp activity, SampleApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;

        // we will reuse this toast
        floodToast = Toast.makeText(mActivity, "" + (minFloodHeightMeters + (floodHeight - minFloodHeight) / (maxFloodHeight - minFloodHeight) * (maxFloodHeightMeters - minFloodHeightMeters)) + " meters.", Toast.LENGTH_LONG);

        line_vertices_array = new ArrayList<double[]>();
        vertices = new ArrayList<double[]>();

        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new SampleAppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 10f, 5000f);
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call function to update rendering when render surface
        // parameters have changed:
        mActivity.updateRendering();

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        mSampleAppRenderer.onConfigurationChanged(mIsActive);

        // Call function to initialize rendering:
        initRendering();
    }


    public void setActive(boolean active) {
        mIsActive = active;

        if (mIsActive)
            mSampleAppRenderer.configureVideoBackground();
    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        // Call our function to render content from SampleAppRenderer class
        mSampleAppRenderer.render();
    }


    // The render function called from SampleAppRendering by using RenderingPrimitives views.
    // The state is owned by SampleAppRenderer which is controlling it's lifecycle.
    // State should not be cached outside this method.
    public void renderFrame(State state, float[] projectionMatrix) {
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Render the RefFree UI elements depending on the current state
        mActivity.refFreeFrame.render();

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);

            Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(trackableResult.getPose());
            modelViewMatrix = modelViewMatrix_Vuforia.getData();

            drawViewFinder(trackableResult, projectionMatrix);

            // draw the supervised lines
            double[] result = processAndDrawLines(trackableResult, projectionMatrix);

            drawVertices(trackableResult, projectionMatrix);
            drawAnnotations(trackableResult, projectionMatrix);

            drawAnnulus(trackableResult, projectionMatrix);
            drawOcclusionGeometry(trackableResult, projectionMatrix);
            drawFloodPlane(trackableResult, projectionMatrix);

//            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
//
//            float[] modelViewProjection = new float[16];
//            Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, kObjectScale);
//            Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale,
//                kObjectScale);
//            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);
//
//            GLES20.glUseProgram(shaderProgramID);
//
//            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
//                false, 0, mTeapot.getVertices());
//            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
//                GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
//
//            GLES20.glEnableVertexAttribArray(vertexHandle);
//            GLES20.glEnableVertexAttribArray(textureCoordHandle);
//
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
//                mTextures.get(0).mTextureID[0]);
//            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
//                modelViewProjection, 0);
//            GLES20.glUniform1i(texSampler2DHandle, 0);
//            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
//                mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
//                mTeapot.getIndices());
//
//            GLES20.glDisableVertexAttribArray(vertexHandle);
//            GLES20.glDisableVertexAttribArray(textureCoordHandle);

            SampleUtils.checkGLError("FloodApp renderFrame");
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        Renderer.getInstance().end();
    }


    private void initRendering() {
        Log.d(LOGTAG, "initRendering");

//        mTeapot = new Teapot();
        mPlane1 = new PlaneObject();
        mPlane2 = new PlaneObject(1, 0, 0, 0, 0, 0, 5, 5, 1);
        mSphere = new SphereObject();
        mCube = new CubeObject();

        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);

        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures) {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, t.mWidth, t.mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(CubeShaders.CUBE_MESH_VERTEX_SHADER, CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
        // PH
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "texSampler2D");
    }


    public void setTextures(Vector<Texture> textures) {
        mTextures = textures;

    }


    /*

            application specific functions follow

     */


    // mode selection (selected from the sliding menu)
    public void triangulatePoints() {
        Log.v("MYINFO", "triangulatePoints");
        showFlood = false;
        occGeomVisible = true;
        primaryMode = Mode.TRIANGULATEPOINTS;
    }

    public void clearGeometry() {
        Log.v("MYINFO", "clearGeometry");
        vertices.clear();
        occgeom.clear();
    }

    public void deletePoints() {
        Log.v("MYINFO", "deletePoints");
        showFlood = false;
        occGeomVisible = true;
        primaryMode = Mode.DELETEPOINTS;
    }

    public void addOcclusionGeometry() {
        Log.v("MYINFO", "addOcclusionGeometry");

        occGeomType = 0;

        new AlertDialog.Builder(mActivity)
                .setTitle("Add Geometry")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                occGeomType = 0;
                                break;

                            case 1:
                                occGeomType = 1;
                                break;

                            case 2:
                                occGeomType = 2;
                                break;
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing !
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        showFlood = false;
                        occGeomVisible = true;
                        primaryMode = Mode.ADDOCCLUSIONGEOMETRY;
                    }
                })
                .show();
    }

    public void setMinHeight() {
        minFloodHeight = floodHeight;

        if (minFloodHeight > maxFloodHeight) {
            double temp = minFloodHeight;

            minFloodHeight = maxFloodHeight;
            maxFloodHeight = temp;
        }

        Log.v("MYINFO", "minFloodHeight = " + minFloodHeight);

        txtedit = new EditText(mActivity);

        new AlertDialog.Builder(mActivity)
                .setTitle("Set Water Level")
                .setMessage("Enter water level in meters.")
                .setView(txtedit)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String meters = txtedit.getText().toString();
                        minFloodHeightMeters = Double.parseDouble(meters.toString());
                    }
                })
                .show();

        Log.v("MYINFO", "minFloodHeightMeters = " + minFloodHeightMeters);
    }

    public void setMaxHeight() {
        maxFloodHeight = floodHeight;

        if (minFloodHeight > maxFloodHeight) {
            double temp = minFloodHeight;

            minFloodHeight = maxFloodHeight;
            maxFloodHeight = temp;
        }

        Log.v("MYINFO", "maxFloodHeight = " + maxFloodHeight);

        txtedit = new EditText(mActivity);

        new AlertDialog.Builder(mActivity)
                .setTitle("Set Water Level")
                .setMessage("Enter water level in meters.")
                .setView(txtedit)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String meters = txtedit.getText().toString();
                        maxFloodHeightMeters = Double.parseDouble(meters.toString());
                    }
                })
                .show();

        Log.v("MYINFO", "maxFloodHeightMeters = " + maxFloodHeightMeters);

    }

    public void scrollFlood(double dx, double dy) {
        if (showFlood) {
            floodHeight += dy * 0.075f;

            // don't restrict flood height

			if(floodHeight > maxFloodHeight)
				floodHeight = maxFloodHeight;

			if(floodHeight < minFloodHeight)
				floodHeight = minFloodHeight;

            double meters = (minFloodHeightMeters + (floodHeight - minFloodHeight) / (maxFloodHeight - minFloodHeight) * (maxFloodHeightMeters - minFloodHeightMeters));

            DecimalFormat newFormat = new DecimalFormat("#.##");
            double twoDecimal = Double.valueOf(newFormat.format(meters));

            floodToast.setText("" + twoDecimal + " meters.");
            floodToast.show();
        }
    }

    public void toggleOccGeomVisible() {
        occGeomVisible = !occGeomVisible;
    }

    static int cast(MeshObject obj) {
        if (obj instanceof OcclusionGeometryBlock)
            return 0;

        if (obj instanceof OcclusionGeometryArch)
            return 1;

        return -1;
    }

    public void toggleShowFloodPlane() {
        // compute the width of the flood plane as a function of occlusion geometry
        double left = occgeom.get(0).v1[0];
        double right = occgeom.get(0).v2[0];
        double front = occgeom.get(0).v1[2] - occgeom.get(0).back;
        double back = occgeom.get(0).v2[2] - occgeom.get(0).back;

        for (MeshObject geom : occgeom) {
            if (left > geom.v1[0])
                left = geom.v1[0];

            if (right < geom.v2[0])
                right = geom.v2[0];

            if (front < geom.v1[2])
                front = geom.v1[2];

            if (back > geom.v2[2] - geom.back)
                back = geom.v2[2] - geom.back;
        }

        floodPlaneWidth = Math.abs(right - left) + floodPadding;
        floodPlaneDepth = Math.abs(front - back) + floodPadding;
        floodPlaneXOffset = left + (floodPlaneWidth / 2) - (floodPadding / 2);
        floodPlaneZOffset = back + (floodPlaneDepth / 2);

        showFlood = !showFlood;
    }

    public void scaleUp() {
        Log.v("MYINFO", "scaleUp");
        secondaryMode = Mode.STRETCH_TOP_SURFACE;
        primaryMode = Mode.SELECTOCCLUSIONGEOMETRY;

        Toast.makeText(mActivity, "Select geometry to scale.", Toast.LENGTH_LONG).show();
    }

    public void scaleDown() {
        Log.v("MYINFO", "scaleDown");
        secondaryMode = Mode.STRECTH_BOTTOM_SURFACE;
        primaryMode = Mode.SELECTOCCLUSIONGEOMETRY;

        Toast.makeText(mActivity, "Select geometry to scale.", Toast.LENGTH_LONG).show();
    }

    public void scaleBack() {
        Log.v("MYINFO", "scaleBack");
        secondaryMode = Mode.STRETCH_BACK_SURFACE;
        primaryMode = Mode.SELECTOCCLUSIONGEOMETRY;

        Toast.makeText(mActivity, "Select geometry to scale.", Toast.LENGTH_LONG).show();
    }

    public void scaleFront() {
        Log.v("MYINFO", "scaleFront");
        secondaryMode = Mode.STRETCH_FRONT_SURFACE;
        primaryMode = Mode.SELECTOCCLUSIONGEOMETRY;

        Toast.makeText(mActivity, "Select geometry to scale.", Toast.LENGTH_LONG).show();
    }

    public void scaleBase() {
        Log.v("MYINFO", "scaleBase");
        secondaryMode = Mode.CHANGE_ARCH_WIDTH;
        primaryMode = Mode.SELECTOCCLUSIONGEOMETRY;

        Toast.makeText(mActivity, "Select geometry to scale.", Toast.LENGTH_LONG).show();
    }

    public void addAnnotation() {
        Log.v("MYINFO", "addAnnotation");
        primaryMode = Mode.SELECTANNOTATIONPOINT;
    }

    // actions performed depending on the current mode
    public void scaleGeometry(float[] line_start, float[] cam_lookat) {
        double[] startp = {line_start[0] + cam_lookat[0] * 75, line_start[1] + cam_lookat[1] * 75, line_start[2] + cam_lookat[2] * 75};
        double[] endp = {line_start[0] + cam_lookat[0] * 475, line_start[1] + cam_lookat[1] * 475, line_start[2] + cam_lookat[2] * 475};

        // ray intersection with plane
        double t = (1 - startp[2]) / (endp[2] - startp[2]);

        // coordinate in plane
        double[] coord = {startp[0] + t * (endp[0] - startp[0]), startp[1] + t * (endp[1] - startp[1]), startp[2] + t * (endp[2] - startp[2])};

        // projection of occ geom coord onto plane
        double[] proj = {occgeom.get(selectedOcclusionGeometry).v1[0], occgeom.get(selectedOcclusionGeometry).v1[1], 0};

        double dx = coord[0] - proj[0];
        double adx = Math.abs(dx);

        double dy = coord[1] - proj[1];
        double ady = Math.abs(dy);

        if (adx < 0.01) adx = 0.01;
        if (ady < 0.01) ady = 0.01;

        Log.v("MYINFO", "adx = " + adx + ", ady = " + ady + ", dx = " + dx);

        // scale the occlusion geometry
        switch (secondaryMode) {
            case STRETCH_TOP_SURFACE:
                occgeom.get(selectedOcclusionGeometry).top = ady;
                break;

            case STRECTH_BOTTOM_SURFACE:
                occgeom.get(selectedOcclusionGeometry).bot = ady;
                break;

            case CHANGE_ARCH_WIDTH:
                occgeom.get(selectedOcclusionGeometry).archwidth = adx;
                break;

            default:
                break;
        }

        // scale
        doScaling(occgeom.get(selectedOcclusionGeometry));
    }

    public void doScaling(MeshObject obj) {
        // scale
        switch (cast(occgeom.get(selectedOcclusionGeometry))) {
            case 0:
                ((OcclusionGeometryBlock) obj).fillVertexBuffer();
                break;

            case 1:
                ((OcclusionGeometryArch) obj).fillVertexBuffer();
                break;

            default:
        }
    }

    public void stretchGeometryBack(double dy) {
        // change the scale value
        occgeom.get(selectedOcclusionGeometry).back += dy;

        // scale
        doScaling(occgeom.get(selectedOcclusionGeometry));
    }

    public void stretchGeometryFront(double dy) {
        // change the scale value
        occgeom.get(selectedOcclusionGeometry).front -= dy;

        // scale
        doScaling(occgeom.get(selectedOcclusionGeometry));
    }

    public void processStretches(double dx, double dy)
    {
        switch(secondaryMode)
        {
            case STRETCH_FRONT_SURFACE:
                stretchGeometryFront(dy);
                break;

            case STRETCH_BACK_SURFACE:
                stretchGeometryBack(dy);
                break;
        }
    }

    public void finishScaling()
    {
        primaryMode = Mode.NONE;
        secondaryMode = Mode.NONE;
    }

    public void addLine()
    {
        Log.v("MYINFO", "addLine");
        addline = true;
    }

    public void selectDelete()
    {
        Log.v("MYINFO", "selectDelete");
        selectDelete = true;
    }

    public void selectAnnotationPoint()
    {
        Log.v("MYINFO", "selectAnnotationPoint");
        primaryMode = Mode.ANNOTATIONPOINTSELECTED;
    }

    public void selectOcclusionGeometryFirstPoint()
    {
        Log.v("MYINFO", "selectOccGeomFirstPoint");
        primaryMode = Mode.SELECTFIRSTPOINT;
    }

    public void selectOcclusionGeometrySecondPoint()
    {
        Log.v("MYINFO", "selectOccGeomSecondPoint");
        primaryMode = Mode.SELECTSECONDPOINT;
    }

    public void defineFloodPlane()
    {
        Log.v("MYINFO", "defineFloodPlane");
        primaryMode = Mode.DEFINEFLOODPLANE;

        Toast.makeText(mActivity, "Select geometry to which flood plane will be oriented.", Toast.LENGTH_LONG).show();
    }

    public void computeFloodPlaneAngles()
    {
        // compute the flood plane angles...

        Log.v("MYINFO", "compute flood plane angles here !!");

        floodAngle = occgeom.get(selectedOcclusionGeometry).v2[0] - occgeom.get(selectedOcclusionGeometry).v1[0];

        floodAngle /= Math.sqrt((occgeom.get(selectedOcclusionGeometry).v2[0] - occgeom.get(selectedOcclusionGeometry).v1[0])*
                (occgeom.get(selectedOcclusionGeometry).v2[0] - occgeom.get(selectedOcclusionGeometry).v1[0])

                + (occgeom.get(selectedOcclusionGeometry).v2[1] - occgeom.get(selectedOcclusionGeometry).v1[1])*
                (occgeom.get(selectedOcclusionGeometry).v2[1] - occgeom.get(selectedOcclusionGeometry).v1[1]));

        floodAngle = Math.acos(floodAngle);

        Log.v("MYINFO", "flood angle = " + floodAngle);

        primaryMode = Mode.NONE;
        secondaryMode = Mode.NONE;
    }

    public void processNone()
    {
        if(showFlood && !occGeomVisible)
        {
            primaryMode = Mode.SELECTANNOTATIONTODISPLAY;
        }
    }

    // code
    double [] processAndDrawLines(TrackableResult trackableResult, float [] projectionMatrix)
    {
        // *************************************
        // *** draw supervised line geometry ***
        // *************************************

        float [] modelViewProjection = new float[16];

        modelViewMatrix44F = Tool.convertPose2GLMatrix(trackableResult.getPose());
        modelViewMatrixInverse44F = SampleMath.Matrix44FInverse(modelViewMatrix44F);
        modelViewMatrixInverseTranspose44F = SampleMath.Matrix44FTranspose(modelViewMatrix44F);

        modelViewMatrix = modelViewMatrix44F.getData(); // we do this+following to easily scale the model view matrix
        Matrix.translateM(modelViewMatrix, 0, 0, 0, kObjectScale);
        Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale, kObjectScale);
        modelViewMatrix44F.setData(modelViewMatrix); // replace the 44F matrix with the transformed float [] matrix

        // PH
        projectionMatrix44F.setData(projectionMatrix);
        projectionMatrixInverse44F = SampleMath.Matrix44FInverse(projectionMatrix44F);
        projectionMatrixInverseTranspose44F = SampleMath.Matrix44FTranspose(projectionMatrixInverse44F);

        // work out camera position and direction
        modelViewMatrixInverse = modelViewMatrixInverseTranspose44F.getData();

        float [] position = {0, 0, 0, 1};
        float [] lookAt = {0, 0, 1, 0};
        float [] cam_position = new float[16];
        float [] cam_lookat = new float[16];

        Matrix.multiplyMV(cam_position, 0, modelViewMatrixInverse, 0, position, 0);
        Matrix.multiplyMV(cam_lookat, 0, modelViewMatrixInverse, 0, lookAt, 0);

        Vec3F line_start = SampleMath.getPointToPlaneLineStart(projectionMatrixInverse44F, modelViewMatrix44F, 3*kObjectScale, 3*kObjectScale, new Vec2F(0, 0), new Vec3F(0, 0, 0), new Vec3F(0, 0, 1));
        // compute normal start and end vectors and compute the unit direction vector

        if(addline)
            addNewLine(line_start.getData(), cam_lookat);

        if(bipoint_counter == 0 && primaryMode == Mode.SELECTFIRSTPOINT)
            selectFirstPoint(line_start.getData(), cam_lookat);

        if(bipoint_counter == 0 && primaryMode == Mode.SELECTSECONDPOINT)
            selectSecondPoint(line_start.getData(), cam_lookat);

        if(bipoint_counter == 0 && primaryMode == Mode.SELECTOCCLUSIONGEOMETRY)
            selectOcclusionGeometry(line_start.getData(), cam_lookat);

        if(bipoint_counter == 0 && primaryMode == Mode.DEFINEFLOODPLANE)
            selectOcclusionGeometryForFloodPlane(line_start.getData(), cam_lookat);

        if(bipoint_counter == 0 && primaryMode == Mode.ANNOTATIONPOINTSELECTED)
            selectAnnotationPoint(line_start.getData(), cam_lookat);

        if(bipoint_counter == 0 && primaryMode == Mode.SCALING)
            scaleGeometry(line_start.getData(), cam_lookat);

        if(bipoint_counter == 0 && primaryMode == Mode.SELECTANNOTATIONTODISPLAY)
            selectAnnotationToDisplay(line_start.getData(), cam_lookat);

        if(bipoint_counter == 0 && selectDelete)
            deletePoint(line_start.getData(), cam_lookat);

        // create line vertex buffer
        line_vertices_buffer = ByteBuffer.allocateDirect(3 * 4 * (2 + line_vertices_array.size()));
        line_vertices_buffer.order(ByteOrder.LITTLE_ENDIAN);

        line_colours_buffer = ByteBuffer.allocateDirect(4 * 4 * (2 + line_vertices_array.size()));
        line_colours_buffer.order(ByteOrder.LITTLE_ENDIAN);

        // add the active line...
//    	line_vertices_buffer.putFloat(line_start.getData()[0]);
//    	line_vertices_buffer.putFloat(line_start.getData()[1]);
//    	line_vertices_buffer.putFloat(line_start.getData()[2]);
//    	line_vertices_buffer.putFloat(line_start.getData()[0] + cam_lookat[0] * 10);
//    	line_vertices_buffer.putFloat(line_start.getData()[1] + cam_lookat[1] * 10);
//    	line_vertices_buffer.putFloat(line_start.getData()[2] + cam_lookat[2] * 10);
        // ... and it's colour data
//    	line_colours_buffer.putFloat(1f);	// colour for line start
//    	line_colours_buffer.putFloat(0f);
//    	line_colours_buffer.putFloat(0f);
//    	line_colours_buffer.putFloat(1f);
//    	line_colours_buffer.putFloat(0f);	// colour for line start
//    	line_colours_buffer.putFloat(1f);
//    	line_colours_buffer.putFloat(0f);
//    	line_colours_buffer.putFloat(1f);

        // loop through start/end line points (they are stored alternately in the array)
        for(double [] floats : line_vertices_array)
        {
            // add previous lines...
            line_vertices_buffer.putFloat((float)floats[0]);
            line_vertices_buffer.putFloat((float)floats[1]);
            line_vertices_buffer.putFloat((float)floats[2]);

            // ... and their colours
            line_colours_buffer.putFloat(1f);
            line_colours_buffer.putFloat(0f);
            line_colours_buffer.putFloat(0f);
            line_colours_buffer.putFloat(1f);
        }

        line_vertices_buffer.rewind();
        line_colours_buffer.rewind();

        // open GL stuff
        GLES20.glUseProgram(shaderProgramID);

        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, line_vertices_buffer.asFloatBuffer());
        GLES20.glEnableVertexAttribArray(vertexHandle);

        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, line_colours_buffer.asFloatBuffer());
        GLES20.glEnableVertexAttribArray(colorHandle);
//        GLES20.glBindAttribLocation(shaderProgramID, colorHandle, "keyColor");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(2).mTextureID[0]);

        Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
//        GLES20.glUniform4fv(colorHandle, 1, line_colours_buffer.asFloatBuffer());
        GLES20.glLineWidth(30);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2 + line_vertices_array.size());
        GLES20.glDisable(GLES20.GL_BLEND);

        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);

        return result;
    }

    void addNewLine(float [] line_start, float [] cam_lookat)
    {
        if(addline)		// add a new line?
        {
            addline = false;

            double [] startp = {line_start[0] + cam_lookat[0] * 75, line_start[1] + cam_lookat[1] * 75, line_start[2] + cam_lookat[2] * 75};
            double [] endp = {line_start[0] + cam_lookat[0] * 475, line_start[1] + cam_lookat[1] * 475, line_start[2] + cam_lookat[2] * 475};

            if(data.size() == 0)
                data.add(new ArrayList<double []>());

            data.get(data.size()-1).add(startp);
            data.get(data.size()-1).add(endp);
            line_vertices_array.add(startp);
            line_vertices_array.add(endp);

            // now triangulate with the new line added !
            result = mymath.triangulate3D(line_vertices_array);

            bipoint_counter++;

            if(bipoint_counter == 3)
            {
                bipoint_counter = 0;
                vertices.add(result);

                Log.v("MYTRI", "final tripoint = (" + result[0] + ", " + result[1] + ", " + result[2] + ")");
                pointDataStr += "(" + result[0] + ", " + result[1] + ", " + result[2] + ")\n";

                data.add(new ArrayList<double []>());		// add a new point
                line_vertices_array.clear();				// clear current list
            }
        }
    }

    void selectAnnotationToDisplay(float [] line_start, float [] cam_lookat)
    {
        double [] startp = {line_start[0] + cam_lookat[0] * 75, line_start[1] + cam_lookat[1] * 75, line_start[2] + cam_lookat[2] * 75};
        double [] endp = {line_start[0] + cam_lookat[0] * 475, line_start[1] + cam_lookat[1] * 475, line_start[2] + cam_lookat[2] * 475};

        double proximity = 10000.0d;
        vert = -1;
        double dist = 10000.0d;
        annot = null;

        // find closest annotation to line
        for(Annotation a : annotations)
        {
            dist = mymath.shortestDistance(startp, endp, vertices.get(a.vertex));

            if(dist < proximity)
            {
                proximity = dist;
                annot = a;
            }
        }

        // if we're outside of bounds then deselect
        if(proximity > 4d)
        {
            vert = -1;
            vertold = -1;
            line_vertices_array.clear();		// finished
            vertfirst = -1;

            annot = null;
            primaryMode = Mode.NONE;
        }

        if(annot != null)
        {
            primaryMode = Mode.NONE;

            // here you would find the sensor tokens within the annotation, download the appropriate
            // sensor information from your API, and merge mail the annotation before displaying it
            // but for this public release we just show the original non-parsed annotation

            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(mActivity)
                            .setTitle("Information")
                            .setMessage(annot.annotationString)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();
                }
            });
        }
    }

    void selectAnnotationPoint(float [] line_start, float [] cam_lookat)
    {
        double [] startp = {line_start[0] + cam_lookat[0] * 75, line_start[1] + cam_lookat[1] * 75, line_start[2] + cam_lookat[2] * 75};
        double [] endp = {line_start[0] + cam_lookat[0] * 475, line_start[1] + cam_lookat[1] * 475, line_start[2] + cam_lookat[2] * 475};

        double proximity = 10000.0d;
        int counter = 0;
        vert = -1;
        double dist = 10000.0d;

        // find closest vertex to line
        for(double [] v : vertices)
        {
            dist = mymath.shortestDistance(startp, endp, v);

            if(dist < proximity)
            {
                proximity = dist;
                vert = counter;
            }

            counter++;
        }

        // if we're outside of bounds then deselect
        if(proximity > 4d)
        {
            vert = -1;
            vertold = -1;
            line_vertices_array.clear();		// finished
            vertfirst = -1;

            primaryMode = Mode.NONE;
        }

        if(vert != -1)
        {
            primaryMode = Mode.NONE;

            if(FloodApp.bMenuAlwaysOut)
                FloodApp.bMenuAlwaysOut = false;

            annotTitleString = "New Annotation";
            annotEditString = "";
            annotIndex = -1;

            for(int c1 = 0; c1 < annotations.size(); c1++)
            {
                if(annotations.get(c1).vertex == vert)
                {
                    annotTitleString = "Amend Annotation";
                    annotEditString = annotations.get(c1).annotationString;
                    annotIndex = c1;
                }
            }

            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    txtedit = new EditText(mActivity);
                    txtedit.setText(annotEditString);

                    if(annotEditString.compareTo("") == 0)
                        txtedit.setHint("The #sh.154.160 sensor is showing a water level of #latestValue.");

                    new AlertDialog.Builder(mActivity)
                            .setTitle(annotTitleString)
                            .setMessage("Enter annotation string to display. Sensor #ID/#parameters can be found here: http://sil-dev.wesenseit.softwaremind.pl/sensor-integration-layer/api-docs/#!/rest")
                            .setView(txtedit)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    // do nothing !
                                }
                            })
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    annotationText = txtedit.getText().toString();
                                    annotationSensor = "";

                                    if(annotIndex == -1)
                                    {
                                        annotations.add(new Annotation(vert, annotationText, annotationSensor));
                                        Toast.makeText(mActivity, "Annotation added:\n\n" + annotationText, Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        annotations.get(annotIndex).annotationString = annotationText;
                                        Toast.makeText(mActivity, "Annotation amended:\n\n" + annotationText, Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .show();
                }
            });
        }
    }

    void selectFirstPoint(float [] line_start, float [] cam_lookat)
    {
        double [] startp = {line_start[0] + cam_lookat[0] * 75, line_start[1] + cam_lookat[1] * 75, line_start[2] + cam_lookat[2] * 75};
        double [] endp = {line_start[0] + cam_lookat[0] * 475, line_start[1] + cam_lookat[1] * 475, line_start[2] + cam_lookat[2] * 475};

        Log.v("MYINFO", "selectFirstPoint");

        double proximity = 10000.0d;
        int counter = 0;
        vert = -1;
        double dist = 10000.0d;

        // find closest vertex to line
        for(double [] v : vertices)
        {
            dist = mymath.shortestDistance(startp, endp, v);

            if(dist < proximity)
            {
                proximity = dist;
                vert = counter;
            }

            counter++;
        }

        // if we're outside of bounds then deselect
        if(proximity > 4d)
        {
            vert = -1;
            vertold = -1;
            line_vertices_array.clear();		// finished
            vertfirst = -1;

            primaryMode = Mode.NONE;
        }

        if(vert != -1)
        {
            first_vert = vert;
            primaryMode = Mode.ADDOCCLUSIONGEOMETRY2;
        }
    }

    void selectSecondPoint(float [] line_start, float [] cam_lookat)
    {
        double [] startp = {line_start[0] + cam_lookat[0] * 75, line_start[1] + cam_lookat[1] * 75, line_start[2] + cam_lookat[2] * 75};
        double [] endp = {line_start[0] + cam_lookat[0] * 475, line_start[1] + cam_lookat[1] * 475, line_start[2] + cam_lookat[2] * 475};

        double proximity = 10000.0d;
        int counter = 0;
        vert = -1;
        double dist = 10000.0d;

        // find closest vertex to line
        for(double [] v : vertices)
        {
            dist = mymath.shortestDistance(startp, endp, v);

            if(dist < proximity)
            {
                proximity = dist;
                vert = counter;
            }

            counter++;
        }

        // if we're outside of bounds then deselect
        if(proximity > 4d)
        {
            vert = -1;
            vertold = -1;
            line_vertices_array.clear();		// finished
            vertfirst = -1;

            primaryMode = Mode.NONE;
        }

        if(vert != -1)
        {
            second_vert = vert;
            primaryMode = Mode.NONE;

            Log.v("MYINFO", "occ geom added.");

            switch(occGeomType)
            {
                case 0:
                    Log.v("MYINFO", "Added block");
                    occgeom.add(new OcclusionGeometryBlock(vertices.get(first_vert), vertices.get(second_vert)));
                    break;

                case 1:
                    Log.v("MYINFO", "Added arch");
                    occgeom.add(new OcclusionGeometryArch(vertices.get(first_vert), vertices.get(second_vert)));
                    break;
            }
        }
    }

    void selectOcclusionGeometry(float [] line_start, float [] cam_lookat)
    {
        double [] startp = {line_start[0] + cam_lookat[0] * 75, line_start[1] + cam_lookat[1] * 75, line_start[2] + cam_lookat[2] * 75};
        double [] endp = {line_start[0] + cam_lookat[0] * 475, line_start[1] + cam_lookat[1] * 475, line_start[2] + cam_lookat[2] * 475};

        double proximity = 10000.0d;
        int counter = 0;
        vert = -1;
        double dist = 10000.0d;

        // find closest vertex to line
        for(MeshObject geom : occgeom)
        {
            dist = mymath.shortestDistance(startp, endp, geom.v1);

            if(dist < proximity)
            {
                proximity = dist;
                vert = counter;
            }

            counter++;
        }

        // if we're outside of bounds then deselect
        if(proximity > 15d)
        {
            vert = -1;
            vertold = -1;
            line_vertices_array.clear();		// finished
            vertfirst = -1;
            selectedOcclusionGeometry = vert;
        }

        if(vert != -1)
        {
            selectedOcclusionGeometry = vert;
        }
    }

    void selectOcclusionGeometryForFloodPlane(float [] line_start, float [] cam_lookat)
    {
        double [] startp = {line_start[0] + cam_lookat[0] * 75, line_start[1] + cam_lookat[1] * 75, line_start[2] + cam_lookat[2] * 75};
        double [] endp = {line_start[0] + cam_lookat[0] * 475, line_start[1] + cam_lookat[1] * 475, line_start[2] + cam_lookat[2] * 475};

        double proximity = 10000.0d;
        int counter = 0;
        vert = -1;
        double dist = 10000.0d;

        // find closest vertex to line
        for(MeshObject geom : occgeom)
        {
            dist = mymath.shortestDistance(startp, endp, geom.v1);

            if(dist < proximity)
            {
                proximity = dist;
                vert = counter;
            }

            counter++;
        }

        // if we're outside of bounds then deselect
        if(proximity > 15d)
        {
            vert = -1;
            vertold = -1;
            line_vertices_array.clear();		// finished
            vertfirst = -1;
            selectedOcclusionGeometry = vert;
        }

        if(vert != -1)
        {
            selectedOcclusionGeometry = vert;
        }
    }

    void occlusionGeometrySelected()
    {
        if(selectedOcclusionGeometry == -1)
        {
            Toast.makeText(mActivity, "No occlusion geometry selection. Operation cancelled", Toast.LENGTH_LONG);

            primaryMode = Mode.NONE;
            secondaryMode = Mode.NONE;

            return;
        }

        switch(secondaryMode)
        {
            case STRETCH_BACK_SURFACE:
                Toast.makeText(mActivity, "Touchscreen up/down to scale.", Toast.LENGTH_LONG).show();

                break;

            case STRETCH_FRONT_SURFACE:
                if(occgeom.get(selectedOcclusionGeometry) instanceof OcclusionGeometryArch)
                {
                    Toast.makeText(mActivity, "Cannot scale an arch in that way.", Toast.LENGTH_LONG).show();

                    primaryMode = Mode.NONE;
                    secondaryMode = Mode.NONE;
                }
                else
                    Toast.makeText(mActivity, "Touchscreen up/down to scale.", Toast.LENGTH_LONG).show();

                break;

            case STRETCH_TOP_SURFACE:
                if(occgeom.get(selectedOcclusionGeometry) instanceof OcclusionGeometryArch)
                {
                    Toast.makeText(mActivity, "Cannot scale an arch in that way.", Toast.LENGTH_LONG).show();

                    primaryMode = Mode.NONE;
                    secondaryMode = Mode.NONE;
                }
                else
                    Toast.makeText(mActivity, "Move cross-hair up/down to scale top surface.", Toast.LENGTH_LONG).show();

                break;

            case STRECTH_BOTTOM_SURFACE:
                Toast.makeText(mActivity, "Move cross-hair up/down to scale bottom surface.", Toast.LENGTH_LONG).show();
                break;

            case CHANGE_ARCH_WIDTH:
                if(occgeom.get(selectedOcclusionGeometry) instanceof OcclusionGeometryBlock) {
                    primaryMode = Mode.NONE;
                    secondaryMode = Mode.NONE;
                    Toast.makeText(mActivity, "Cannot scale a cube like that.", Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(mActivity, "Touchscreen up/down to scale.", Toast.LENGTH_LONG).show();

                break;


            default:
                break;
        }

        primaryMode = Mode.SCALING;
    }

    void deletePoint(float [] line_start, float [] cam_lookat)
    {
        selectDelete = false;

        double [] startp = {line_start[0] + cam_lookat[0] * 75, line_start[1] + cam_lookat[1] * 75, line_start[2] + cam_lookat[2] * 75};
        double [] endp = {line_start[0] + cam_lookat[0] * 475, line_start[1] + cam_lookat[1] * 475, line_start[2] + cam_lookat[2] * 475};

        double proximity = 10000.0d;
        int counter = 0;
        vert = -1;
        double dist = 10000.0d;

        // find closest vertex to line
        for(double [] v : vertices)
        {
            dist = mymath.shortestDistance(startp, endp, v);

            if(dist < proximity)
            {
                proximity = dist;
                vert = counter;
                vertold = vert;
            }

            counter++;
        }

        if(vert != -1)
        {
            vertices.remove(vert);
        }

        vert = -1;
        vertold = -1;

        // TODO: *** we should remove all geometry associated with this vertex too ***
    }

    // graphics related functions

    void drawViewFinder(TrackableResult trackableResult, float [] projectionMatrix)
    {
        if(occGeomVisible && !showFlood)
        {
            Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(trackableResult.getPose());
            float[] modelViewProjection = new float[16];

            modelViewMatrix = modelViewMatrix_Vuforia.getData();

            Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, kObjectScale);
            Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale, kObjectScale);
            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

            GLES20.glUseProgram(shaderProgramID);

            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mPlane1.getVertices());
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, mPlane1.getNormals());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mPlane1.getTexCoords());

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

            GLES20.glEnable(GLES20.GL_CULL_FACE);				// use culling to remove back faces !
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);				// enable depth testing !
            GLES20.glEnable(GLES20.GL_BLEND);					// Enable blending using pre-multiplied alpha.
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(0).mTextureID[0]);
            Matrix.scaleM(modelViewProjection, 0, 60, 45, 1);

            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, mPlane1.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, mPlane1.getIndices());

            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);
            GLES20.glDisable(GLES20.GL_BLEND);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
    }

    void drawAnnulus(TrackableResult trackableResult, float [] projectionMatrix)
    {
        float [] modelViewProjection = new float[16];
        float [] point = new float[3];

        modelViewMatrix44F = Tool.convertPose2GLMatrix(trackableResult.getPose());
        modelViewMatrixInverse44F = SampleMath.Matrix44FInverse(modelViewMatrix44F);
        modelViewMatrixInverseTranspose44F = SampleMath.Matrix44FTranspose(modelViewMatrix44F);

        modelViewMatrix = modelViewMatrix44F.getData(); // we do this+following to easily scale the model view matrix
        Matrix.translateM(modelViewMatrix, 0, 0, 0, kObjectScale);
        Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale, kObjectScale);
        modelViewMatrix44F.setData(modelViewMatrix); // replace the 44F matrix with the transformed float [] matrix

        projectionMatrix44F.setData(projectionMatrix);
        projectionMatrixInverse44F = SampleMath.Matrix44FInverse(projectionMatrix44F);
        projectionMatrixInverseTranspose44F = SampleMath.Matrix44FTranspose(projectionMatrixInverse44F);

        // work out camera position and direction
        modelViewMatrixInverse = modelViewMatrixInverseTranspose44F.getData();

        float [] position = {0, 0, 0, 1};
        float [] lookAt = {0, 0, 1, 0};
        float [] cam_position = new float[16];
        float [] cam_lookat = new float[16];

        Matrix.multiplyMV(cam_position, 0, modelViewMatrixInverse, 0, position, 0);
        Matrix.multiplyMV(cam_lookat, 0, modelViewMatrixInverse, 0, lookAt, 0);

        Vec3F line_start = SampleMath.getPointToPlaneLineStart(projectionMatrixInverse44F, modelViewMatrix44F, 2*kObjectScale, 2*kObjectScale, new Vec2F(0, 0), new Vec3F(0, 0, 0), new Vec3F(0, 0, 1));

        GLES20.glUseProgram(shaderProgramID);

        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mPlane2.getInstance().getVertices());
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, mPlane2.getInstance().getNormals());
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mPlane2.getInstance().getTexCoords());

        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(9).mTextureID[0]);

        point[0] = line_start.getData()[0] + cam_lookat[0] * 80;
        point[1] = line_start.getData()[1] + cam_lookat[1] * 80;
        point[2] = line_start.getData()[2] + cam_lookat[2] * 80;

        Matrix.translateM(modelViewMatrix, 0, point[0], point[1], point[2]);
        Matrix.rotateM(modelViewMatrix, 0, 0, 1, 0, 0);
        Matrix.rotateM(modelViewMatrix, 0, 0, 0, 1, 0);
        Matrix.rotateM(modelViewMatrix, 0, 0, 0, 0, 1);
        Matrix.scaleM(modelViewMatrix, 0, 3, 3, 3);

        Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
        GLES20.glUniform1i(texSampler2DHandle, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mPlane2.getInstance().getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, mPlane2.getInstance().getIndices());
        GLES20.glDisable(GLES20.GL_BLEND);

        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
    }

    void drawVertices(TrackableResult trackableResult, float [] projectionMatrix)
    {
        if(occGeomVisible)
        {
            Palette [] colours = {Palette.RED, Palette.GREEN, Palette.BLUE};

            for(int c1 = 0; c1 < vertices.size(); c1++)
            {
                if(c1 == vert)
                    drawSphere(trackableResult, vertices.get(c1), Palette.WHITE, 2.5f, projectionMatrix);
                else
                    drawSphere(trackableResult, vertices.get(c1), colours[c1 % 3], 1f, projectionMatrix);
            }
        }
    }

    void drawAnnotations(TrackableResult trackableResult, float [] projectionMatrix)
    {
        float radius = 1f;

        if(showFlood && !occGeomVisible)
            radius = 3f;

        for(Annotation a : annotations)
            drawAnnotationCube(trackableResult, vertices.get(a.vertex), radius, projectionMatrix);
    }

    void drawFloodPlane(TrackableResult trackableResult, float [] projectionMatrix)
    {
        if(showFlood)
        {
            Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(trackableResult.getPose());
            float[] modelViewProjection = new float[16];

            modelViewMatrix = modelViewMatrix_Vuforia.getData();

            Matrix.rotateM(modelViewMatrix, 0, -90, 1, 0, 0);
            Matrix.translateM(modelViewMatrix, 0, 0.0f, 0, (float)floodHeight);
            Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale, kObjectScale);
            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

            GLES20.glUseProgram(shaderProgramID);

            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mPlane1.getVertices());
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, mPlane1.getNormals());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mPlane1.getTexCoords());

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

//	 	    GLES20.glEnable(GLES20.GL_CULL_FACE);				// use culling to remove back faces !
            GLES20.glDisable(GLES20.GL_CULL_FACE);				// use culling to remove back faces !
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);				// enable depth testing !
            GLES20.glEnable(GLES20.GL_BLEND);					// Enable blending using pre-multiplied alpha.
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(3).mTextureID[0]);
            Matrix.rotateM(modelViewProjection, 0, (float) (floodAngle / 3.141592654 * 180.0), 0, -1, 0);
            Matrix.translateM(modelViewProjection, 0, (float)floodPlaneXOffset, -(float)floodPlaneZOffset, 0);
            Matrix.scaleM(modelViewProjection, 0, (float)floodPlaneWidth, (float)floodPlaneDepth, 1);

            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, mPlane1.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, mPlane1.getIndices());

            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);
            GLES20.glDisable(GLES20.GL_BLEND);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
    }

    void drawAnnotationCube(TrackableResult trackableResult, double [] point, float radius, float [] projectionMatrix)
    {
        // draw annotation spheres

        float [] modelViewProjection = new float[16];

        modelViewMatrix44F = Tool.convertPose2GLMatrix(trackableResult.getPose());
        modelViewMatrix = modelViewMatrix44F.getData(); // we do this+following to easily scale the model view matrix
        Matrix.translateM(modelViewMatrix, 0, 0, 0, kObjectScale);
        Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale, kObjectScale);
        GLES20.glUseProgram(shaderProgramID);
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mCube.getInstance().getVertices());
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, mCube.getInstance().getNormals());
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mCube.getInstance().getTexCoords());

        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(11).mTextureID[0]);

        Matrix.translateM(modelViewMatrix, 0, (float)point[0], (float)point[1], (float)point[2]);
        Matrix.rotateM(modelViewMatrix, 0, 0, 1, 0, 0);

        angle += 2;
        if(angle > 359)
            angle = 0;

        if(showFlood && !occGeomVisible)
            Matrix.rotateM(modelViewMatrix, 0, angle, 0, 1, 0);
        else
            Matrix.rotateM(modelViewMatrix, 0, 0, 0, 1, 0);

        Matrix.rotateM(modelViewMatrix, 0, 0, 0, 0, 1);
        Matrix.scaleM(modelViewMatrix, 0, radius, radius, radius);

        Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
        GLES20.glUniform1i(texSampler2DHandle, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mCube.getInstance().getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, mCube.getInstance().getIndices());
        GLES20.glDisable(GLES20.GL_BLEND);

        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
    }

    void drawSphere(TrackableResult trackableResult, double [] point, Palette c, float radius, float [] projectionMatrix)
    {
        // ***********************************
        // *** draw the triangulated point ***
        // ***********************************

        float [] modelViewProjection = new float[16];

        modelViewMatrix44F = Tool.convertPose2GLMatrix(trackableResult.getPose());
        modelViewMatrix = modelViewMatrix44F.getData(); // we do this+following to easily scale the model view matrix
        Matrix.translateM(modelViewMatrix, 0, 0, 0, kObjectScale);
        Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale, kObjectScale);
        GLES20.glUseProgram(shaderProgramID);
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mSphere.getInstance().getVertices());
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, mSphere.getInstance().getNormals());
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mSphere.getInstance().getTexCoords());

        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        switch(c)
        {
            case RED:
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(5).mTextureID[0]);
                break;

            case GREEN:
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(6).mTextureID[0]);
                break;

            case BLUE:
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(7).mTextureID[0]);
                break;

            case WHITE:
            default:
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(8).mTextureID[0]);
                break;
        }

        Matrix.translateM(modelViewMatrix, 0, (float)point[0], (float)point[1], (float)point[2]);
        Matrix.rotateM(modelViewMatrix, 0, 0, 1, 0, 0);
        Matrix.rotateM(modelViewMatrix, 0, 0, 0, 1, 0);
        Matrix.rotateM(modelViewMatrix, 0, 0, 0, 0, 1);
        Matrix.scaleM(modelViewMatrix, 0, radius, radius, radius);

        Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
        GLES20.glUniform1i(texSampler2DHandle, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mSphere.getInstance().getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, mSphere.getInstance().getIndices());
        GLES20.glDisable(GLES20.GL_BLEND);

        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
    }

    void drawTriangles(TrackableResult trackableResult, ArrayList<double []> points, ArrayList<Integer> edges, float [] projectionMatrix)
    {
        float [] modelViewProjection = new float[16];

        modelViewMatrix44F = Tool.convertPose2GLMatrix(trackableResult.getPose());
        modelViewMatrixInverse44F = SampleMath.Matrix44FInverse(modelViewMatrix44F);
        modelViewMatrixInverseTranspose44F = SampleMath.Matrix44FTranspose(modelViewMatrix44F);

        modelViewMatrix = modelViewMatrix44F.getData(); // we do this+following to easily scale the model view matrix
        Matrix.translateM(modelViewMatrix, 0, 0, 0, kObjectScale);
        Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale, kObjectScale);
        modelViewMatrix44F.setData(modelViewMatrix); // replace the 44F matrix with the transformed float [] matrix

        int num_edges = edges.size() - edges.size() % 3;

        // create line vertex buffer
        triangle_vertices_buffer = ByteBuffer.allocateDirect(3 * 4 * num_edges);	// there are 4 points per set of 3 edges, and edges come in 3's
        triangle_vertices_buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(int c1 = 0; c1 < num_edges; c1++)
        {
            triangle_vertices_buffer.putFloat((float) points.get(edges.get(c1))[0]);
            triangle_vertices_buffer.putFloat((float) points.get(edges.get(c1))[1]);
            triangle_vertices_buffer.putFloat((float) points.get(edges.get(c1))[2]);
        }

        triangle_vertices_buffer.rewind();

        // open GL stuff
        GLES20.glUseProgram(shaderProgramID);

        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, triangle_vertices_buffer.asFloatBuffer());
        GLES20.glEnableVertexAttribArray(vertexHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(8).mTextureID[0]);

        Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, num_edges);
        GLES20.glDisable(GLES20.GL_BLEND);

        GLES20.glDisableVertexAttribArray(vertexHandle);
    }

    void drawOcclusionGeometry(TrackableResult trackableResult, float [] projectionMatrix)
    {
        int counter = 0;

        for(MeshObject geom : occgeom)
        {
            float[] modelViewProjection = new float[16];

            Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(trackableResult.getPose());
            modelViewMatrix = modelViewMatrix_Vuforia.getData();

            Matrix.translateM(modelViewMatrix, 0, 0, 0, kObjectScale);
            Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale, kObjectScale);
            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

            GLES20.glUseProgram(shaderProgramID);

            switch(cast(geom))
            {
                case 0:
                    GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, ((OcclusionGeometryBlock)geom).getVertices());
                    GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, ((OcclusionGeometryBlock)geom).getNormals());
                    GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, ((OcclusionGeometryBlock)geom).getTexCoords());
                    break;

                case 1:
                    GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, ((OcclusionGeometryArch)geom).getVertices());
                    GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, ((OcclusionGeometryArch)geom).getNormals());
                    GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, ((OcclusionGeometryArch)geom).getTexCoords());
                    break;
            }

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

            if(occGeomVisible)
            {
                if(selectedOcclusionGeometry == counter)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(2).mTextureID[0]);
                else
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(1).mTextureID[0]);
            }
            else
            {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(4).mTextureID[0]);
            }

            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);

            switch(cast(geom))
            {
                case 0:
                    GLES20.glDrawElements(GLES20.GL_TRIANGLES, ((OcclusionGeometryBlock)geom).getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, ((OcclusionGeometryBlock)geom).getIndices());
                    break;

                case 1:
                    GLES20.glDrawElements(GLES20.GL_TRIANGLES, ((OcclusionGeometryArch)geom).getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, ((OcclusionGeometryArch)geom).getIndices());
                    break;
            }

            GLES20.glDisable(GLES20.GL_BLEND);

            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);

            counter++;
        }
    }

    public class Annotation
    {
        int vertex;
        String annotationString;
        String sensorString;

        Annotation(int vert, String text, String sensor)
        {
            vertex = vert;
            annotationString = text;
            sensorString = sensor;
        }
    }
}
