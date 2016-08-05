package com.pl.webplibrary;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Factory to encode and decode WebP images into Android Bitmap
 * @author Alexey Pelykh
 */
public final class BitmapFactory {
	static {
		System.loadLibrary("webpbackport");
	}

	/**
	 * Decodes byte array to bitmap 
	 * @param data Byte array with WebP bitmap data
	 * @param options Options to control decoding. Accepts null
	 * @return Decoded bitmap
	 */
	public static native Bitmap nativeDecodeByteArray(byte[] data, android.graphics.BitmapFactory.Options options);

	/**
     * Decodes file to bitmap
     *
     * @param path    WebP file path
     * @param options Options to control decoding. Accepts null
     * @return Decoded bitmap
     */
    private static native Bitmap nativeDecodeFile(String path, android.graphics.BitmapFactory.Options options);


	private static byte[] streamToBytes(InputStream is) {
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = is.read(buffer)) >= 0) {
				os.write(buffer, 0, len);
			}
		} catch (java.io.IOException e) {
		}
		return os.toByteArray();
	}


	public static Bitmap decodeFile(String pathName, android.graphics.BitmapFactory.Options opts) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return android.graphics.BitmapFactory.decodeFile(pathName, opts);
		}else {
			if (!pathName.toLowerCase().endsWith("webp")) {
				android.graphics.BitmapFactory.decodeFile(pathName, opts);
			}
			return nativeDecodeFile(pathName, opts);
		}
	}

	/**
	 * Decode a file path into a bitmap. If the specified file name is null,
	 * or cannot be decoded into a bitmap, the function returns null.
	 *
	 * @param pathName complete path name for the file to be decoded.
	 * @return the resulting decoded bitmap, or null if it could not be decoded.
	 */
	public static Bitmap decodeFile(String pathName) {
		return decodeFile(pathName, null);
	}

	/**
	 * Decode a new Bitmap from an InputStream. This InputStream was obtained from
	 * resources, which we pass to be able to scale the bitmap accordingly.
	 */
	public static Bitmap decodeResourceStream(Resources res, TypedValue value,
											  InputStream is, Rect pad, android.graphics.BitmapFactory.Options opts) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return android.graphics.BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
		}else {
			String name = res.getString(value.resourceId);
			if (!name.toLowerCase().endsWith("webp")) {
				return android.graphics.BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
			}
			if (opts == null) {
				opts = new android.graphics.BitmapFactory.Options();
			}
			if (opts.inDensity == 0 && value != null) {
				final int density = value.density;
				if (density == TypedValue.DENSITY_DEFAULT) {
					opts.inDensity = DisplayMetrics.DENSITY_DEFAULT;
				} else if (density != TypedValue.DENSITY_NONE) {
					opts.inDensity = density;
				}
			}
			if (opts.inTargetDensity == 0 && res != null) {
				opts.inTargetDensity = res.getDisplayMetrics().densityDpi;
			}
			return decodeStream(is, pad, opts);
		}
	}

	/**
	 * Synonym for opening the given resource and calling
	 * {@link #decodeResourceStream}.
	 *
	 * @param res   The resources object containing the image data
	 * @param id The resource id of the image data
	 * @param opts null-ok; Options that control downsampling and whether the
	 *             image should be completely decoded, or just is size returned.
	 * @return The decoded bitmap, or null if the image data could not be
	 *         decoded, or, if opts is non-null, if opts requested only the
	 *         size be returned (in opts.outWidth and opts.outHeight)
	 */
	public static Bitmap decodeResource(Resources res, int id, android.graphics.BitmapFactory.Options opts) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return android.graphics.BitmapFactory.decodeResource(res, id, opts);
		}else {
			String name = res.getString(id);
			if (!name.toLowerCase().endsWith("webp")) {
				return android.graphics.BitmapFactory.decodeResource(res, id, opts);
			}

			Bitmap bm = null;
			InputStream is = null;

			try {
				final TypedValue value = new TypedValue();
				is = res.openRawResource(id, value);

				bm = decodeResourceStream(res, value, is, null, opts);
			} catch (Exception e) {
            /*  do nothing.
                If the exception happened on open, bm will be null.
                If it happened on close, bm is still valid.
            */
			} finally {
				try {
					if (is != null) is.close();
				} catch (IOException e) {
					// Ignore
				}
			}

			if (bm == null && opts != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					if (opts.inBitmap != null)
						throw new IllegalArgumentException("Problem decoding into existing bitmap");
				} else {
					throw new IllegalArgumentException("Problem decoding into existing bitmap");
				}
			}
			return bm;
		}
	}

	/**
	 * Synonym for {@link #decodeResource(Resources, int, android.graphics.BitmapFactory.Options)}
	 * with null Options.
	 *
	 * @param res The resources object containing the image data
	 * @param id The resource id of the image data
	 * @return The decoded bitmap, or null if the image could not be decoded.
	 */
	public static Bitmap decodeResource(Resources res, int id) {
		return decodeResource(res, id, null);
	}

	/**
	 * Decode an immutable bitmap from the specified byte array.
	 *
	 * @param data byte array of compressed image data
	 * @param offset offset into imageData for where the decoder should begin
	 *               parsing.
	 * @param length the number of bytes, beginning at offset, to parse
	 * @param opts null-ok; Options that control downsampling and whether the
	 *             image should be completely decoded, or just is size returned.
	 * @return The decoded bitmap, or null if the image data could not be
	 *         decoded, or, if opts is non-null, if opts requested only the
	 *         size be returned (in opts.outWidth and opts.outHeight)
	 */
	public static Bitmap decodeByteArray(byte[] data, int offset, int length, android.graphics.BitmapFactory.Options opts) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return android.graphics.BitmapFactory.decodeByteArray(data, offset, length, opts);
		}else {
			if ((offset | length) < 0 || data.length < offset + length) {
				throw new ArrayIndexOutOfBoundsException();
			}
			Bitmap bm;
			byte[] newData = data;
			if (offset != 0) {
				newData = new byte[length];
				System.arraycopy(data, offset, newData, 0, length);
			}
			try {
				byte[] webp = new byte[16];
				System.arraycopy(data, 0, webp, 0, 16);
				String string = new String(webp);
				if (string.toLowerCase().contains("webp")) {
					bm = nativeDecodeByteArray(data, opts);
				} else {
					return android.graphics.BitmapFactory.decodeByteArray(data, 0, data.length, opts);
				}
				if (bm == null && opts != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						if (opts.inBitmap != null)
							throw new IllegalArgumentException("Problem decoding into existing bitmap");
					} else {
						throw new IllegalArgumentException("Problem decoding into existing bitmap");
					}
				}
				setDensityFromOptions(bm, opts);
			} finally {
			}

			return bm;
		}
	}

	/**
	 * Decode an immutable bitmap from the specified byte array.
	 *
	 * @param data byte array of compressed image data
	 * @param offset offset into imageData for where the decoder should begin
	 *               parsing.
	 * @param length the number of bytes, beginning at offset, to parse
	 * @return The decoded bitmap, or null if the image could not be decoded.
	 */
	public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
		return decodeByteArray(data, offset, length, null);
	}

	/**
	 * Set the newly decoded bitmap's density based on the Options.
	 */
	private static void setDensityFromOptions(Bitmap outputBitmap, android.graphics.BitmapFactory.Options opts) {
		if (outputBitmap == null || opts == null) return;

		final int density = opts.inDensity;
		if (density != 0) {
			outputBitmap.setDensity(density);
			final int targetDensity = opts.inTargetDensity;
			if (targetDensity == 0 || density == targetDensity || density == opts.inScreenDensity) {
				return;
			}

			byte[] np = outputBitmap.getNinePatchChunk();
			final boolean isNinePatch = np != null && NinePatch.isNinePatchChunk(np);
			if (opts.inScaled || isNinePatch) {
				outputBitmap.setDensity(targetDensity);
			}
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				if( opts.inBitmap != null)
					outputBitmap.setDensity(getDefaultDensity());
			}else {
				throw new IllegalArgumentException("Problem decoding into existing bitmap");
			}
			// bitmap was reused, ensure density is reset
			outputBitmap.setDensity(getDefaultDensity());
		}
	}
	static int sDefaultDensity=-1;

	private static int getDefaultDensity(){
		if (sDefaultDensity>=0){
			return sDefaultDensity;
		}else {
			try {
				Class<Bitmap> bitmapClass= (Class<Bitmap>) Class.forName("android.graphics.Bitmap");
				Method method=bitmapClass.getDeclaredMethod("getDefaultDensity");
				method.setAccessible(true);
				sDefaultDensity= (Integer) method.invoke(null);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return sDefaultDensity;
		}
	}

	/**
	 * Decode an input stream into a bitmap. If the input stream is null, or
	 * cannot be used to decode a bitmap, the function returns null.
	 * The stream's position will be where ever it was after the encoded data
	 * was read.
	 *
	 * @param is The input stream that holds the raw data to be decoded into a
	 *           bitmap.
	 * @param outPadding If not null, return the padding rect for the bitmap if
	 *                   it exists, otherwise set padding to [-1,-1,-1,-1]. If
	 *                   no bitmap is returned (null) then padding is
	 *                   unchanged.
	 * @param opts null-ok; Options that control downsampling and whether the
	 *             image should be completely decoded, or just is size returned.
	 * @return The decoded bitmap, or null if the image data could not be
	 *         decoded, or, if opts is non-null, if opts requested only the
	 *         size be returned (in opts.outWidth and opts.outHeight)
	 *
	 * <p class="note">Prior to {@link android.os.Build.VERSION_CODES#KITKAT},
	 * if {@link InputStream#markSupported is.markSupported()} returns true,
	 * <code>is.mark(1024)</code> would be called. As of
	 * {@link android.os.Build.VERSION_CODES#KITKAT}, this is no longer the case.</p>
	 */
	public static Bitmap decodeStream(InputStream is, Rect outPadding, android.graphics.BitmapFactory.Options opts) {
		// we don't throw in this case, thus allowing the caller to only check
		// the cache, and not force the image to be decoded.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return android.graphics.BitmapFactory.decodeStream(is, outPadding, opts);
		}else {
			if (is == null) {
				return null;
			}
			Bitmap bm = null;
			try {
				bm = decodeStreamInternal(is, outPadding, opts);
				if (bm == null && opts != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						if (opts.inBitmap != null)
							throw new IllegalArgumentException("Problem decoding into existing bitmap");
					} else {
						throw new IllegalArgumentException("Problem decoding into existing bitmap");
					}
				}
				setDensityFromOptions(bm, opts);
			} finally {
			}
			return bm;
		}
	}

	/**
	 * Private helper function for decoding an InputStream natively. Buffers the input enough to
	 * do a rewind as needed, and supplies temporary storage if necessary. is MUST NOT be null.
	 */
	private static Bitmap decodeStreamInternal(InputStream is, Rect outPadding, android.graphics.BitmapFactory.Options opts) {
		// ASSERT(is != null);
		Bitmap bm;
		byte[] data = streamToBytes(is);
		if (is instanceof FileInputStream){
			byte[] webp=new byte[16];
			System.arraycopy(data,0,webp,0,16);
			String string=new String(webp);
			if (string.toLowerCase().contains("webp")){
                bm = nativeDecodeByteArray(data, opts);
            }else {
                return android.graphics.BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            }
		}else {
			bm = nativeDecodeByteArray(data, opts);
		}
		return bm;
	}

	/**
	 * Decode an input stream into a bitmap. If the input stream is null, or
	 * cannot be used to decode a bitmap, the function returns null.
	 * The stream's position will be where ever it was after the encoded data
	 * was read.
	 *
	 * @param is The input stream that holds the raw data to be decoded into a
	 *           bitmap.
	 * @return The decoded bitmap, or null if the image data could not be decoded.
	 */
	public static Bitmap decodeStream(InputStream is) {
		return decodeStream(is, null, null);
	}

	/**
	 * Decode a bitmap from the file descriptor. If the bitmap cannot be decoded
	 * return null. The position within the descriptor will not be changed when
	 * this returns, so the descriptor can be used again as-is.
	 *
	 * @param fd The file descriptor containing the bitmap data to decode
	 * @param outPadding If not null, return the padding rect for the bitmap if
	 *                   it exists, otherwise set padding to [-1,-1,-1,-1]. If
	 *                   no bitmap is returned (null) then padding is
	 *                   unchanged.
	 * @param opts null-ok; Options that control downsampling and whether the
	 *             image should be completely decoded, or just its size returned.
	 * @return the decoded bitmap, or null
	 */
	public static Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, android.graphics.BitmapFactory.Options opts) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return android.graphics.BitmapFactory.decodeFileDescriptor(fd, outPadding, opts);
		}else {
			Bitmap bm;
			try {
				FileInputStream fis = new FileInputStream(fd);
				try {
					bm = decodeStream(fis, outPadding, opts);
				} finally {
					try {
						fis.close();
					} catch (Throwable t) {/* ignore */}
				}
			} finally {
			}
			return bm;
		}
	}

	/**
	 * Decode a bitmap from the file descriptor. If the bitmap cannot be decoded
	 * return null. The position within the descriptor will not be changed when
	 * this returns, so the descriptor can be used again as is.
	 *
	 * @param fd The file descriptor containing the bitmap data to decode
	 * @return the decoded bitmap, or null
	 */
	public static Bitmap decodeFileDescriptor(FileDescriptor fd) {
		return decodeFileDescriptor(fd, null, null);
	}

}
