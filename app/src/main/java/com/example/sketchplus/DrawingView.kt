/**
package com.example.sketchplus

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0f
    private var color = Color.CYAN
    private var canvas: Canvas? = null

    init {
        setupDrawing()
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float): Path() {

    }

    private fun setupDrawing(){
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.BUTT
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        if(!mDrawPath!!.isEmpty) {
            mDrawPaint?.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchx = event?.x
        val touchy = event?.y

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset()
                if(touchy != null && touchx != null) {
                    mDrawPath!!.moveTo(touchx,touchy)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if(touchx != null && touchy!= null){
                    mDrawPath!!.moveTo(touchx,touchy)
                }
            }
            MotionEvent.ACTION_UP -> {
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }

        invalidate()
        return true


    }

} **/

package com.example.sketchplus

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

// TODO (Step 3 : Creating a drawing view with some basic features as creating canvas to draw with required attributes
//  and providing a default color and size of stroke.)
// START

/**
 * This class contains the attributes for the main layout of
 * our application.
 */

/**
 * The constructor for ViewForDrawing
 * This constructor calls the setupDrawing()
 * method. This constructor is called only
 * once when the application layout is first
 * created upon launch.
 *
 * @param context
 * @param attrs
 */

/**
 * The reference link to create this class is
 * https://medium.com/@ssaurel/learn-to-create-a-paint-application-for-android-5b16968063f8
 */
@SuppressLint("NewApi")
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawPath: CustomPath? =
        null // An variable of CustomPath inner class to use it further.
    var mCanvasBitmap: Bitmap? = null // An instance of the Bitmap.

    var mDrawPaint: Paint? =
        null // The Paint class holds the style and color information about how to draw geometries, text and bitmaps.
    var mCanvasPaint: Paint? = null // Instance of canvas paint view.

    var mBrushSize: Float =
        0.toFloat() // A variable for stroke/brush size to draw on the canvas.

    private val mPaths = ArrayList<CustomPath>()
    //this will hold all the path variables that will persist on the screen later on

    // A variable to hold a color of the stroke.
    var color = Color.parseColor("#FF000000")

    //An arraylist to hold all the custompaths so that the last added path can be removed on undo click
    private var mUndoPaths = ArrayList<CustomPath>()

    /**
     * A variable for canvas which will be initialized later and used.
     *
     *The Canvas class holds the "draw" calls. To draw something, you need 4 basic components: A Bitmap to hold the pixels, a Canvas to host
     * the draw calls (writing into the bitmap), a drawing primitive (e.g. Rect,
     * Path, text, Bitmap), and a paint (to describe the colors and styles for the
     * drawing)
     */
    private var canvas: Canvas? = null

    init {
        setUpDrawing()
    }

    fun onClickUndo() {
        if(mPaths.size > 0) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
            invalidate()
        }
    }

    /**
     * This method initializes the attributes of the
     * ViewForDrawing class.
     */
    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)

        mDrawPaint!!.color = color

        mDrawPaint!!.style = Paint.Style.STROKE // This is to draw a STROKE style
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND // This is for store join
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND // This is for stroke Cap

        mCanvasPaint = Paint(Paint.DITHER_FLAG) // Paint flag that enables dithering when blitting.

        mBrushSize =
            20.toFloat() // Here the default or we can initial brush/ stroke size is defined.
    }

    override fun onSizeChanged(w: Int, h: Int, wprev: Int, hprev: Int) {
        super.onSizeChanged(w, h, wprev, hprev)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    /**
     * This method is called when a stroke is drawn on the canvas
     * as a part of the painting.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for(path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        /**
         * Draw the specified bitmap, with its top/left corner at (x,y), using the specified paint,
         * transformed by the current matrix.
         *
         *If the bitmap and canvas have different densities, this function will take care of
         * automatically scaling the bitmap to draw at the same density as the canvas.
         *
         * @param bitmap The bitmap to be drawn
         * @param left The position of the left side of the bitmap being drawn
         * @param top The position of the top side of the bitmap being drawn
         * @param paint The paint used to draw the bitmap (may be null)
         */
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    /**
     * This method acts as an event listener when a touch
     * event is detected on the device.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x // Touch event of X coordinate
        val touchY = event.y // touch event of Y coordinate

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset() // Clear any lines and curves from the path, making it empty.
                mDrawPath!!.moveTo(
                    touchX,
                    touchY
                ) // Set the beginning of the next contour to the point (x,y).
            }

            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(
                    touchX,
                    touchY
                ) // Add a line from the last point to the specified point (x,y).
            }

            MotionEvent.ACTION_UP -> {
                mDrawPath = CustomPath(color, mBrushSize)

                //this line adds the path to arraylist the moment finger is lifted up the screen
                mPaths.add(mDrawPath!!)
            }
            else -> return false
        }

        invalidate()
        return true
    }

    //This function will help to set the size of the brush
    fun setBrushSize (newSize: Float) {

        //usually, values in pixels mean different thickness interpretation in different device sizes,
        //hence using this method we can convert the value in dps to corresponding scaled displayMetrics

        /**
         * @param unit = what should be the unit to be converted
         * @param size = numeric value of the corresponding unit in float
         * @param metrics = value of display metrics that includes various screen parameters necessary to calculate
         */
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize, resources.displayMetrics)

        mDrawPaint!!.strokeWidth = mBrushSize
    }

    // An inner class for custom path with two params as color and stroke size.
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path()
}
// END