package com.pl.webplib;

import android.app.Activity;
import android.backport.webp.WebPFactory;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SampleActivity extends Activity {
	private final int REQUEST_CODE__IMAGE_SELECTED = 1;
	private final int REQUEST_CODE__IMAGE_CAPTURED = 2;

	public static final int[] DEFAULT_PIC_NAME= {
			R.drawable.default_pic_1,R.drawable.default_pic_2,
			R.drawable.default_pic_3,R.drawable.default_pic_4,
			R.drawable.default_pic_5,R.drawable.default_pic_6,
			R.drawable.default_pic_7,R.drawable.default_pic_8,
			R.drawable.default_pic_9,R.drawable.default_pic_10,
			R.drawable.default_pic_11,R.drawable.default_pic_12,
			R.drawable.default_pic_13,R.drawable.default_pic_14,
			R.drawable.default_pic_15,R.drawable.default_pic_16,
			R.drawable.default_pic_17,R.drawable.default_pic_18,
			R.drawable.default_pic_19,R.drawable.default_pic_20,
			R.drawable.default_pic_21,R.drawable.default_pic_22,
	};

	private int index=0;

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

	ImageView _imageView = null;

	File _captureDestination = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		_imageView = (ImageView) findViewById(R.id.imageView);

		final Button loadEmbeddedImageButton = (Button) findViewById(R.id.loadEmbeddedImage);
		final Button loadImageFromFile = (Button) findViewById(R.id.loadImageFromFile);
		loadEmbeddedImageButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				InputStream rawImageStream = getResources().openRawResource(R.raw.image);
				byte[] data = streamToBytes(rawImageStream);
				final Bitmap webpBitmap = bitmapf.decodeByteArray(
						data, 0,data.length,null);
				_imageView.setImageBitmap(webpBitmap);

//				InputStream inputStream=getResources().openRawResource(DEFAULT_PIC_NAME[index++%DEFAULT_PIC_NAME.length]);
//				Bitmap webpBitmap=WebPFactory.decodeResource(getResources(),DEFAULT_PIC_NAME[index++%DEFAULT_PIC_NAME.length]);
//				_imageView.setImageBitmap(webpBitmap);

//				_imageView.setImageResource(DEFAULT_PIC_NAME[index++%DEFAULT_PIC_NAME.length]);
//				String name=getResources().getResourceName(DEFAULT_PIC_NAME[index%DEFAULT_PIC_NAME.length]);
//				AssetManager assetManager=getAssets();
//				Toast.makeText(SampleActivity.this,name,Toast.LENGTH_SHORT).show();
			}
		});

		loadImageFromFile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bitmap webpBitmap= null;
				try {
					webpBitmap = WebPFactory.decodeFileDescriptor(new FileInputStream(getFilesDir()+"sample.webp").getFD());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				_imageView.setImageBitmap(webpBitmap);
			}
		});


		copyAssetsToData();
	}

	private void copyAssetsToData(){
		try {
			InputStream is=getResources().getAssets().open("sample.webp");
			IOUtil.copy(is,getFilesDir()+"sample.webp");
			is.close();

			is=getResources().getAssets().open("sample2.jpg");
			IOUtil.copy(is,getFilesDir()+"sample2.jpg");
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
		case REQUEST_CODE__IMAGE_SELECTED:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = intent.getData();

				Bitmap selectedBitmap;

				// Try pre-KitKat approach
				Cursor cursor = getContentResolver().query(selectedImage,
						new String[] { MediaStore.Images.Media.DATA }, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				String filePath = cursor.getString(columnIndex);
				cursor.close();

				if (filePath != null) {
					selectedBitmap = BitmapFactory.decodeFile(filePath);
				} else {
					ParcelFileDescriptor imageFd = null;
					try {
						imageFd = getContentResolver().openFileDescriptor(selectedImage, "r");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					FileDescriptor imageSource = imageFd.getFileDescriptor();

					selectedBitmap = BitmapFactory.decodeFileDescriptor(imageSource);
				}

//				byte[] webpImageData = WebPFactory.nativeEncodeBitmap(selectedBitmap, 100);
//				try {
//					FileOutputStream dumpStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "dump.webp"));
//					dumpStream.write(webpImageData);
//					dumpStream.close();
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				Bitmap webpBitmap = WebPFactory.nativeDecodeByteArray(webpImageData, null);
//				_imageView.setImageBitmap(webpBitmap);
			}
			break;
		case REQUEST_CODE__IMAGE_CAPTURED:
			if (resultCode == RESULT_OK) {
				Bitmap selectedBitmap = BitmapFactory.decodeFile(_captureDestination.getAbsolutePath());
//				byte[] webpImageData = WebPFactory.nativeEncodeBitmap(selectedBitmap, 100);
//				try {
//					FileOutputStream dumpStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "dump.webp"));
//					dumpStream.write(webpImageData);
//					dumpStream.close();
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				Bitmap webpBitmap = WebPFactory.nativeDecodeByteArray(webpImageData, null);
//				_imageView.setImageBitmap(webpBitmap);
//
//				_captureDestination = null;
			}
			break;
		}
	}
}