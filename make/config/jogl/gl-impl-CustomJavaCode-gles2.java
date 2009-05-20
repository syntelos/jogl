// Tracks glBegin/glEnd calls to determine whether it is legal to
// query Vertex Buffer Object state
private boolean inBeginEndPair;

public GLES2Impl(GLContextImpl context) {
  this._context = context; 
  this.bufferSizeTracker = context.getBufferSizeTracker();
}

public final boolean isGL() {
    return true;
}
  
public final boolean isGL2() {
    return false;
}

public final boolean isGLES1() {
    return false;
}

public final boolean isGLES2() {
    return true;
}

public final boolean isGLES() {
    return true;
}

public final boolean isGL2ES1() {
    return false;
}

public final boolean isGL2ES2() {
    return true;
}

public final GL getGL() throws GLException {
    return this;
}

public final GL2 getGL2() throws GLException {
    throw new GLException("Not a GL2 implementation");
}

public final GLES1 getGLES1() throws GLException {
    throw new GLException("Not a GLES1 implementation");
}

public final GLES2 getGLES2() throws GLException {
    return this;
}

public final GL2ES1 getGL2ES1() throws GLException {
    throw new GLException("Not a GL2ES1 implementation");
}

public final GL2ES2 getGL2ES2() throws GLException {
    return this;
}

public boolean isFunctionAvailable(String glFunctionName) {
  return _context.isFunctionAvailable(glFunctionName);
}

public boolean isExtensionAvailable(String glExtensionName) {
  return _context.isExtensionAvailable(glExtensionName);
}

public Object getExtension(String extensionName) {
  // At this point we don't expose any extensions using this mechanism
  return null;
}

/** Returns the context this GL object is associated with for better
    error checking by DebugGL. */
public GLContext getContext() {
  return _context;
}

private GLContextImpl _context;

/**
 * Provides platform-independent access to the wglAllocateMemoryNV /
 * glXAllocateMemoryNV extension.
 */
public java.nio.ByteBuffer glAllocateMemoryNV(int arg0, float arg1, float arg2, float arg3) {
  return _context.glAllocateMemoryNV(arg0, arg1, arg2, arg3);
}

public void setSwapInterval(int interval) {
  _context.setSwapInterval(interval);
}

public Object getPlatformGLExtensions() {
  return _context.getPlatformGLExtensions();
}

//
// Helpers for ensuring the correct amount of texture data
//

/** Returns the number of bytes required to fill in the appropriate
    texture. This is regrettably a lower bound as in certain
    circumstances OpenGL state such as unpack alignment can cause more
    data to be required. However this should be close enough that it
    should catch most crashes. The logic in this routine is based on
    code in the SGI OpenGL sample implementation. */

private int imageSizeInBytes(int format, int type, int w, int h, int d) {
  int elements = 0;
  int esize = 0;
  
  if (w < 0) return 0;
  if (h < 0) return 0;
  if (d < 0) return 0;
  switch (format) {
  case GL_STENCIL_INDEX:
    elements = 1;
    break;
  case GL_ALPHA:
  case GL_LUMINANCE:
  case GL_DEPTH_COMPONENT:
    elements = 1;
    break;
  case GL_LUMINANCE_ALPHA:
    elements = 2;
    break;
  case GL_RGB:
    elements = 3;
    break;
  case GL_RGBA:
    elements = 4;
    break;
  default:
    return 0;
  }
  switch (type) {
  case GL_BYTE:
  case GL_UNSIGNED_BYTE:
    esize = 1;
    break;
  case GL_SHORT:
  case GL_UNSIGNED_SHORT:
    esize = 2;
    break;
  case GL_UNSIGNED_SHORT_5_6_5:
  case GL_UNSIGNED_SHORT_4_4_4_4:
  case GL_UNSIGNED_SHORT_5_5_5_1:
    esize = 2;
    elements = 1;
    break;
  case GL_INT:
  case GL_UNSIGNED_INT:
  case GL_FLOAT:
    esize = 4;
    break;
  default:
    return 0;
  }
  return (elements * esize * w * h * d);
}

private GLBufferStateTracker bufferStateTracker = new GLBufferStateTracker();
private GLBufferSizeTracker  bufferSizeTracker;

private boolean bufferObjectExtensionsInitialized = false;
private boolean haveOESFramebufferObject;

private void initBufferObjectExtensionChecks() {
  if (bufferObjectExtensionsInitialized)
    return;
  bufferObjectExtensionsInitialized = true;
  haveOESFramebufferObject  = isExtensionAvailable("GL_OES_framebuffer_object");
}

private boolean checkBufferObject(boolean avail,
                                  boolean enabled,
                                  int state,
                                  String kind, boolean throwException) {
  if (!avail) {
    if (!enabled)
      return true;
    if(throwException) {
        throw new GLException("Required extensions not available to call this function");
    }
    return false;
  }
  int buffer = bufferStateTracker.getBoundBufferObject(state, this);
  if (enabled) {
    if (buffer == 0) {
      if(throwException) {
          throw new GLException(kind + " must be enabled to call this method");
      }
      return false;
    }
  } else {
    if (buffer != 0) {
      if(throwException) {
          throw new GLException(kind + " must be disabled to call this method");
      }
      return false;
    }
  }
  return true;
}  

private boolean checkArrayVBODisabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  return checkBufferObject(true,
                    false,
                    GL.GL_ARRAY_BUFFER,
                    "array vertex_buffer_object", throwException);
}

private boolean checkArrayVBOEnabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  return checkBufferObject(true,
                    true,
                    GL.GL_ARRAY_BUFFER,
                    "array vertex_buffer_object", throwException);
}

private boolean checkElementVBODisabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  return checkBufferObject(true,
                    false,
                    GL.GL_ELEMENT_ARRAY_BUFFER,
                    "element vertex_buffer_object", throwException);
}

private boolean checkElementVBOEnabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  return checkBufferObject(true,
                    true,
                    GL.GL_ELEMENT_ARRAY_BUFFER,
                    "element vertex_buffer_object", throwException);
}

private boolean checkUnpackPBODisabled(boolean throwException) { 
    // PBO n/a for ES 1.1 or ES 2.0
    return true;
}

private boolean checkUnpackPBOEnabled(boolean throwException) { 
    // PBO n/a for ES 1.1 or ES 2.0
    return false;
}

private boolean checkPackPBODisabled(boolean throwException) { 
    // PBO n/a for ES 1.1 or ES 2.0
    return true;
}

private boolean checkPackPBOEnabled(boolean throwException) { 
    // PBO n/a for ES 1.1 or ES 2.0
    return false;
}

// Attempt to return the same ByteBuffer object from glMapBufferARB if
// the vertex buffer object's base address and size haven't changed
private static class ARBVBOKey {
  private long addr;
  private int  capacity;

  ARBVBOKey(long addr, int capacity) {
    this.addr = addr;
    this.capacity = capacity;
  }

  public int hashCode() {
    return (int) addr;
  }

  public boolean equals(Object o) {
    if ((o == null) || (!(o instanceof ARBVBOKey))) {
      return false;
    }

    ARBVBOKey other = (ARBVBOKey) o;
    return ((addr == other.addr) && (capacity == other.capacity));
  }
}

private Map/*<ARBVBOKey, ByteBuffer>*/ arbVBOCache = new HashMap();

/** Entry point to C language function: <br> <code> LPVOID glMapBuffer(GLenum target, GLenum access); </code>    */
public java.nio.ByteBuffer glMapBuffer(int target, int access) {
  final long __addr_ = ((GLES2ProcAddressTable)_context.getGLProcAddressTable())._addressof_glMapBufferOES;
  if (__addr_ == 0) {
    throw new GLException("Method \"glMapBuffer\" not available");
  }
  int sz = bufferSizeTracker.getBufferSize(bufferStateTracker,
                                           target,
                                           this);
  long addr;
  addr = dispatch_glMapBuffer(target, access, __addr_);
  if (addr == 0 || sz == 0) {
    return null;
  }
  ARBVBOKey key = new ARBVBOKey(addr, sz);
  java.nio.ByteBuffer _res = (java.nio.ByteBuffer) arbVBOCache.get(key);
  if (_res == null) {
    _res = newDirectByteBuffer(addr, sz);
    InternalBufferUtil.nativeOrder(_res);
    arbVBOCache.put(key, _res);
  }
  _res.position(0);
  return _res;
}

/** Encapsulates function pointer for OpenGL function <br>: <code> LPVOID glMapBuffer(GLenum target, GLenum access); </code>    */
native private long dispatch_glMapBuffer(int target, int access, long glProcAddress);

native private ByteBuffer newDirectByteBuffer(long addr, int capacity);

public void glClearDepth(double depth) {
    glClearDepthf((float)depth); 
}

public void glDepthRange(double zNear, double zFar) {
    glDepthRangef((float)zNear, (float)zFar); 
}

