package com.example.quckdraw

import org.junit.Test
import androidx.lifecycle.testing.TestLifecycleOwner
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DrawingViewTests {
    private val vm = DrawingViewModel()
    private val lifecycleOwner = TestLifecycleOwner()
    @Test
    fun doesPenColorChange() {
        val before = vm.penLiveData.value?.color
        var callbackFired = false
        vm.penLiveData.observe(lifecycleOwner){
            callbackFired = true
        }
        vm.setPenColor(10)
        assertTrue(callbackFired)
        assertNotSame(before, vm.penLiveData.value?.color )
    }

    @Test
    fun doesPenSizeChange() {
        val before = vm.penLiveData.value?.size
        var callbackFired = false
        vm.penLiveData.observe(lifecycleOwner){
            callbackFired = true
        }
        vm.setPenSize(1.0f)
        assertTrue(callbackFired)
        assertNotSame(before, vm.penLiveData.value?.size )
    }

    @Test
    fun pathsUpdatedAccordingly() {
        val before = vm.getNumberOfPaths()
        vm.addToPath(1.0f, 1.0f)
        assertNotSame(before, vm.getNumberOfPaths())
    }
    @Test
    fun penShapeDidChange(){
        val before = vm.penLiveData.value?.shape
        var callbackFired = false
        vm.penLiveData.observe(lifecycleOwner){
            callbackFired = true
        }
        vm.setPenShape(Shape.SQUARE)
        assertTrue(callbackFired)
        assertNotSame(before, vm.penLiveData.value?.shape)
        assertSame(Shape.SQUARE, vm.penLiveData.value?.shape)
    }
}