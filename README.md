# NativeImgCompress
图片压缩，jni保存

### 1. 读取图片，只读宽高
```java
  BitmapFactory.Options newOpts = new BitmapFactory.Options();
  newOpts.inJustDecodeBounds = true;//只读边,不读内容  
  BitmapFactory.decodeFile(filePath, newOpts);
  int w = newOpts.outWidth;
  int h = newOpts.outHeight;
```

### 2. 设置大小裁剪比例
```java
  /**
	 * 计算缩放比
	 * @param bitWidth 当前图片宽度
	 * @param bitHeight 当前图片高度
	 * @return int 缩放比
	 */
	public static int getRatioSize(int bitWidth, int bitHeight) {
		// 图片最大分辨率
		int imageHeight = 1280;
		int imageWidth = 960;
		// 缩放比
		int ratio = 1;
		// 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		if (bitWidth > bitHeight && bitWidth > imageWidth) {
			// 如果图片宽度比高度大,以宽度为基准
			ratio = bitWidth / imageWidth;
		} else if (bitWidth < bitHeight && bitHeight > imageHeight) {
			// 如果图片高度比宽度大，以高度为基准
			ratio = bitHeight / imageHeight;
		}
		// 最小比率为1
		if (ratio <= 0)
			ratio = 1;
		return ratio;
	}
```

### 3. 获取裁剪图片后，获取旋转角度
```java
/**
	 *
	 * 读取图片属性：旋转的角度
	 * @param path 图片绝对路径
	 * @return degree旋转的角度
	 */

	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}
```

### 4. 图片旋转生成新的bitmap
```java
int photoDegree = readPictureDegree(filePath);
if(photoDegree != 0){
  Matrix matrix = new Matrix();
  matrix.postRotate(photoDegree);
  // 创建新的图片
  bitmap = Bitmap.createBitmap(bitmap, 0, 0,
  bitmap.getWidth(), bitmap.getHeight(), matrix, true);
}
```

### 5. 质量压缩，native方法保存新图片
```java 
 //根据地址获取bitmap
		Bitmap result = getBitmapFromFile(curFilePath);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int quality = 100;
		result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		// 循环判断如果压缩后图片是否大于500kb,大于继续压缩
		while (baos.toByteArray().length / 1024 > maxSize) {
			// 重置baos即清空baos
			baos.reset();
			// 每次都减少10
			quality -= 10;
			// 这里压缩quality，把压缩后的数据存放到baos中
			result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		}
		// JNI保存图片到SD卡 这个关键
		NativeUtil.saveBitmap(result, quality, targetFilePath, true);
		// 释放Bitmap
		if (!result.isRecycled()) {
			result.recycle();
		}
```

## 参考
> https://github.com/liujinchao/AndroidPicCompress
