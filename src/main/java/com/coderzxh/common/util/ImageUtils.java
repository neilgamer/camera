package com.coderzxh.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageUtils {

	private static Logger logger = LoggerFactory.getLogger(ImageUtils.class);
	/**
	 * 图片转为base64编码
	 * @param imgFile 图片路径
	 * @return
	 */
	public static String GetImageStr(String imgFile) {
		if(imgFile == null || imgFile.trim().length() <= 0) {
			return null;
		}
		File file = new File(imgFile);
		if(!file.isFile()){
			return null;
		}
		try {

			byte[] bytes = toByteArray3(imgFile);
			BASE64Encoder encoder = new BASE64Encoder();
			return encoder.encode(bytes);
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}

	public static byte[] toByteArray1(String filename){
		if(filename == null || filename.trim().length() <= 0) {
			return null;
		}
		InputStream in = null;
		byte[] data = null;
		try {

			in = new FileInputStream(filename);
			data = new byte[in.available()];
			in.read(data);
		} catch (IOException e) {
			logger.error("filename: "+ filename, e);
		}
		return data;
	}

	/**
	 * Mapped File way MappedByteBuffer 可以在处理大文件时，提升性能
	 *
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray3(String filename){
		if(filename == null || filename.trim().length() <= 0) {
			return null;
		}

		FileChannel fc = null;
		try {
			fc = new RandomAccessFile(filename, "r").getChannel();
			MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size()).load();
			byte[] result = new byte[(int) fc.size()];
			if (byteBuffer.remaining() > 0) {
				byteBuffer.get(result, 0, byteBuffer.remaining());
			}
			return result;
		} catch (IOException e) {
			logger.error("filename: "+ filename, e);
		} finally {
			try {
				fc.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		return null;
	}

		/**
         * 将图片的文件地址变成html上img标签可以使用的src字符串
         * @param imgFile 文件地址
         * @return
         */
	public static String getImageHtmlSrc(String imgFile) {
		String s = GetImageStr(imgFile);
		if(StringUtils.isEmpty(s)){
			return s;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("data:image/octet-stream;base64,");
		sb.append(s);
		return sb.toString();
	}
	
	public static boolean GenerateImage(String filePath, String fileData) {
		if (fileData == null) {
			return false;
		}
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			byte[] b = decoder.decodeBuffer(fileData);
			for(int i=0;i<b.length;++i) {
				if(b[i]<0) {
					b[i] += 256;
				}
			}
			String imgFilePath = filePath;
			OutputStream out = new FileOutputStream(imgFilePath); 
			out.write(b);
			out.flush();
			out.close();
			return true;
		} 
		catch (Exception e) {
			return false;
		}
	}
	
	public static boolean GenerateImage(String imgStr) {
		if (imgStr == null) {
			return false;
		}
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			byte[] b = decoder.decodeBuffer(imgStr);
			for(int i=0;i<b.length;++i) {
				if(b[i]<0) {
					b[i]+=256;
				}
			}
			String imgFilePath = "D:\\aaa\\new_test_"+new Date().getTime()+".jpg";//新生成的图片
			OutputStream out = new FileOutputStream(imgFilePath); 
			out.write(b);
			out.flush();
			out.close();
			return true;
		} 
		catch (Exception e) {
			return false;
		}
	}
	
	public static byte[] GetImageByteArrayByBase64String(String imgStr) {
		if (imgStr == null) {
			return null;
		}
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			byte[] b = decoder.decodeBuffer(imgStr);
			for(int i=0;i<b.length;++i) {
				if(b[i]<0) {
					b[i]+=256;
				}
			}
			return b;
		} 
		catch (Exception e) {
			return null;
		}
	}
    
    /**
     * 给图片添加图片水印
     * @param pressImg 水印图片
     * @param srcImageFile 源图像地址
     * @param destImageFile 目标图像地址
     * @param x 修正值。 默认在中间
     * @param y 修正值。 默认在中间
     * @param alpha 透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressImage(String pressImg, String srcImageFile,String destImageFile, int x, int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, width, height, null);
            // 水印文件
            Image src_biao = ImageIO.read(new File(pressImg));
            int wideth_biao = src_biao.getWidth(null);
            int height_biao = src_biao.getHeight(null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            g.drawImage(src_biao, x, y, wideth_biao, height_biao, null);
            // 水印文件结束
            g.dispose();
            ImageIO.write((BufferedImage) image,  "JPEG", new File(destImageFile));
        } catch (Exception e) {
			logger.error("pressImg: "+ pressImg +
					"srcImageFile: "+ srcImageFile +
					"destImageFile: "+ destImageFile , e);
        }
    }
    
    /**
     * 给图片添加垂直文字水印
     * @param pressText 水印文字
     * @param srcImageFile 源图像地址
     * @param destImageFile 目标图像地址
     * @param fontName 水印的字体名称
     * @param fontStyle 水印的字体样式
     * @param color 水印的字体颜色
     * @param fontSize 水印的字体大小
     * @param x 修正值
     * @param y 修正值
     * @param alpha 透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressText_Vertical(String pressText, String srcImageFile, String destImageFile, String fontName, int fontStyle, Color color, int fontSize,int x, int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, width, height, null);
            g.setColor(color);
            Font rotatedFont = new Font(fontName, fontStyle, fontSize);
            
            // AffineTransform affineTransform = new AffineTransform();
            // affineTransform.rotate(Math.toRadians(90), 0, 0);
            // rotatedFont.deriveFont(affineTransform);
            
            g.setFont(rotatedFont);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            
            // 在指定坐标绘制水印文字
            // g.drawString(pressText, (width - (getLength(pressText) * fontSize)) / 2 + x, (height - fontSize) / 2 + y);
            addText_ShuPaiWenzi(width / 2 + x, height / 2 + y, pressText, g);
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));// 输出到文件流
        } catch (Exception e) {
			logger.error("", e);
        }
    }
    
    public static void addText_ShuPaiWenzi(int x,int y,String str,Graphics2D g){
    	int strlength = str.length();
    	//获取字体宽度
    	int w = g.getFontMetrics().stringWidth("str");
    	int h = g.getFontMetrics().getHeight();
    	//竖排文字
    	for(int i=0;i<strlength;i++)	{
    		g.drawString(String.valueOf(str.charAt(i)), x, y);
    		//y +=t;
    		y += h;
    	}
    }
    
    /**
     * 给图片添加水平文字水印
     * @param pressText 水印文字
     * @param srcImageFile 源图像地址
     * @param destImageFile 目标图像地址
     * @param fontName 字体名称
     * @param fontStyle 字体样式
     * @param color 字体颜色
     * @param fontSize 字体大小
     * @param x 修正值
     * @param y 修正值
     * @param alpha 透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressText_Horizon(String pressText, String srcImageFile,String destImageFile, String fontName, int fontStyle, Color color, int fontSize, int x, int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, width, height, null);
            g.setColor(color);
            g.setFont(new Font(fontName, fontStyle, fontSize));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            // 在指定坐标绘制水印文字
            g.drawString(pressText, (width - (getLength(pressText) * fontSize)) / 2 + x, (height - fontSize) / 2 + y);
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));
        } catch (Exception e) {
			logger.error("", e);
        }
    }
    
    /**
     * 水平的文字水印
     * @param pressText 水印文字
     * @param srcImageFile 源图像地址
     * @param destImageFile 目标图像地址
     * @param alpha 透明度
     * @param scale	文字缩放倍数
     * @param startX 开始位置 x 坐标
     * @param startY 开始位置 y 坐标
     * @param fontFilePath 自定义字体文件路径，传 null 值则使用默认字体
     * @param fontStyle	字体样式
     * @param fontSize 字体大小
     * @param lineSpace 行间距
     * @param fontColor 字体颜色
     * @param lineMaxStringLength 每行最大字符数，超出则换行
     */
    public static void pressText_Horizon(String[] pressText, String srcImageFile,String destImageFile, float alpha
    		, float scale, float startX, float startY, String fontFilePath, Integer fontStyle, Integer fontSize
    		, Float lineSpace, String fontColor, Integer lineMaxStringLength) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.black);
            g.drawImage(src, 0, 0, width, height, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            
            if(fontStyle == null) {
            	fontStyle = g.getFont().getStyle();
            }
            if(fontSize == null) {
            	fontSize = g.getFont().getSize();
            }
            if(fontColor == null || fontColor.trim().length() <= 0) {
            	fontColor = Color2String(g.getColor());
            }
            if(lineSpace == null) {
            	lineSpace = 10f;
            }
            
            // 去除字体锯齿
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font font = null;
            if(fontFilePath == null || fontFilePath.trim().length() <= 0) {
            	font = new Font(g.getFont().getFontName(), fontStyle, fontSize);
            } else {
            	font = getFont(fontFilePath, fontStyle, fontSize);
            }
            if(font != null) {
            	g.setFont(font);
            }
            if(fontColor == null || fontColor.trim().length() <= 0) {
            	fontColor = "#000000";		// 缺省为黑色
            }
            
            g.setColor(String2Color(fontColor));
            // 放大倍数
            Font newFont = new Font(g.getFont().getFontName(), g.getFont().getStyle(), (int)(g.getFont().getSize() * scale));
            g.setFont(newFont);
            
            pressText = resettingMaximArray(pressText, g, (int)(width - startX * scale), lineMaxStringLength);
            
            for(int i=0; i < pressText.length; i++) {
            	String text = pressText[i];
            	int wordHeight = g.getFontMetrics().getHeight();
            	int x = (int)(startX * scale);
            	int oy = (int)((startY * scale) + i * wordHeight);
            	int y = oy;
            	if(i > 0) {
            		// 从第二行开始加大行间距
            		// 因为坐标点是从左边开始计算的，而这里的竖排文字又是从右边开始的，所以自定义的补偿间距要使用负数
            		y = (int)(oy + (scale * i * lineSpace));
            	}
            	g.drawString(text, x, y);
            }
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));
        } catch (Exception e) {
			logger.error("pressText: "+ pressText +
					"srcImageFile: "+ srcImageFile +
					"destImageFile: "+ destImageFile , e);
        }
    }
    
    public static String[] resettingMaximArray(String[] maxs, Graphics2D g, int imgWidth, Integer lineMaxStringLength) {
    	maxs = ReBuildArrayByMaxLineStrLength(maxs, lineMaxStringLength);
    	return maxs;
    }
    
//    public static String[] resettingMaximArray(String[] maxs, Graphics2D g, int imgWidth, Integer lineMaxStringLength) {
//    	maxs = ReBuildArrayByMaxLineStrLength(maxs, lineMaxStringLength);
//    	imgWidth = imgWidth - 10;	// 左右留5个像素
//    	int emptyStrLength = g.getFontMetrics().stringWidth(" ");
//    	for(int j=0;j<maxs.length;j++) {
//    		String text = maxs[j];
//    		int result = 0;
//        	for (int i = 0; i < text.length(); i++) {
//                String str = new String(text.charAt(i) + "");
//                result += g.getFontMetrics().stringWidth(str);
//             
//                if(result + emptyStrLength >= imgWidth) {
//                	maxs = pulsStringArray(maxs, j, i);
//                	break;
//            	}
//            }
//    	}
//    	return maxs;
//    }
    
    private static String[] ReBuildArrayByMaxLineStrLength(String[] strs, Integer lineMaxStringLength) {
    	List<String> res = new ArrayList<String>();
    	for(String item : strs) {
    		if(item.length() > lineMaxStringLength) {
    			while(item != null && item.trim().length() > 0) {
    				if(item.length() > lineMaxStringLength) {
    					res.add(item.substring(0, lineMaxStringLength));
    					item = item.substring(lineMaxStringLength);
    				} else {
    					res.add(item);
    					item = null;
    				}
    			}
    		} else {
    			res.add(item);
    		}
    	}
    	return res.toArray(new String[0]);
    }
    
    public static int getStringWidth(String text, Graphics2D g) {
    	int result = 0;
    	for (int i = 0; i < text.length(); i++) {
            String str = new String(text.charAt(i) + "");
            result += g.getFontMetrics().stringWidth(str);
        }
    	return result;
    }
    
    public static String Color2String(Color color) {
    	String R = Integer.toHexString(color.getRed());
    	R = R.length()<2?('0'+R):R;
    	String B = Integer.toHexString(color.getBlue());
		B = B.length()<2?('0'+B):B;
		String G = Integer.toHexString(color.getGreen());
		G = G.length()<2?('0'+G):G;
		return '#'+R+B+G;
    }
    
    /**
     * 
     * 给图片添加垂直文字水印
     * @param pressText 水印文字
     * @param srcImageFile 源图像地址
     * @param destImageFile 目标图像地址
     * @param alpha 透明度
     * @param scale 文字缩放倍数
     * @param startX 开始位置 x 坐标
     * @param startY 开始位置 y 坐标
     * @param fontFilePath 自定义字体文件路径，传 null 值则使用默认字体
     * @param fontStyle	字体样式
     * @param fontSize 字体大小
     * @param lineSpace 行间距
     * @param fontColor 字体颜色
     * @param lineMaxStringLength 每行最大字符数，超出则换行
     */
    public static void pressText_Vertical(String[] pressText, String srcImageFile, String destImageFile, float alpha
    		, float scale, float startX, float startY, String fontFilePath, Integer fontStyle, Integer fontSize
    		, Float lineSpace, String fontColor, Integer lineMaxStringLength) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            
            if(fontStyle == null) {
            	fontStyle = g.getFont().getStyle();
            }
            if(fontSize == null) {
            	fontSize = g.getFont().getSize();
            }
            if(lineSpace == null) {
            	lineSpace = 10f;
            }
            
            // 去除字体锯齿
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font font = null;
            if(fontFilePath == null || fontFilePath.trim().length() <= 0) {
            	font = new Font(g.getFont().getFontName(), fontStyle, fontSize);
            } else {
            	font = getFont(fontFilePath, fontStyle, fontSize);
            }
            if(font != null) {
            	g.setFont(font);
            }
            
            if(fontColor == null || fontColor.trim().length() <= 0) {
            	fontColor = "#000000";		// 缺省为黑色
            }
            g.setColor(String2Color(fontColor));
            g.drawImage(src, 0, 0, width, height, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            pressText = ReBuildArrayByMaxLineStrLength(pressText, lineMaxStringLength);
            addText_VerticalWord(pressText, g, width, height, scale, startX, startY, lineSpace);
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));// 输出到文件流
        } catch (Exception e) {
			logger.error("pressText: "+ pressText +
					"srcImageFile: "+ srcImageFile +
					"destImageFile: "+ destImageFile , e);
        }
    }
    
    public static void addText_VerticalWord(String[] words,Graphics2D g, int imgWidth, int imgHeight, float scale
    		, float startX, float startY, float lineSpace) {
    	Font font = new Font(g.getFont().getFontName(), g.getFont().getStyle(), (int)(g.getFont().getSize() * scale));
        g.setFont(font);
        
    	for(int z=0; z < words.length; z++) {
    		String text = words[z];
        	
        	int w = g.getFontMetrics().stringWidth("str");
        	int h = g.getFontMetrics().getHeight();
        	
        	// int ox = (int)(imgWidth - (startX * scale) - z * w);			// 从右上角算起
        	int ox = (int)((startX * scale) - z * w);						// 从左上角算起
        	int y = (int)(startY * scale);
        	int x = ox;
        	if(z > 0) {
        		// 从第二行开始加大行间距
        		// 因为坐标点是从左边开始计算的，而这里的竖排文字又是从右边开始的，所以自定义的补偿间距要使用负数
        		x = (int)(ox + (scale * z * lineSpace * -1));
        	}
        	for(int i=0; i<text.length(); i++)	{
        		if(y + h >= imgHeight) {
        			// 文字超出底边了
        			// 本行剩余文字放到下一行中，如果是最后一行则增加一行
        			words = pulsStringArray(words, z, i);
        			break;
        		}
        		String wz = String.valueOf(text.charAt(i));
        		int bakX = x;
        		int zmAsc = (int)text.charAt(i);
        		if((zmAsc >= 65 && zmAsc <= 90) || (zmAsc >= 97 && zmAsc <= 122)) {
        			int zww = g.getFontMetrics().stringWidth("中");
        			int zmw = g.getFontMetrics().stringWidth(wz);
        			if(zww - zmw > 0) {
        				x = x + ((zww - zmw) / 2);
        			}
        		}
        		g.drawString(wz, x, y);
        		x = bakX;
        		y += h;
        	}
    	}
    }
    
    private static String[] pulsStringArray(String[] arry, int rowIndex, int colIndex) {
    	String[] res = new String[arry.length];
    	if(rowIndex >= arry.length - 1) {
    		// 增加新行
    		res = new String[arry.length + 1];
    	}
    	for(int i=0;i<arry.length;i++) {
    		res[i] = arry[i];
    	}
    	String s1 = res[rowIndex].substring(0, colIndex);
    	String s2 = res[rowIndex].substring(colIndex);
    	res[rowIndex] = s1;
    	if(res[rowIndex + 1] == null) {
    		res[rowIndex + 1] = s2;
    	} else {
    		res[rowIndex + 1] = s2 + res[rowIndex + 1];
    	}
    	return res;
    }
    
    /**
     * 计算text的长度（一个中文算两个字符）
     * @param text
     * @return
     */
    public static int getLength(String text) {
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            if (new String(text.charAt(i) + "").getBytes().length > 1) {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length / 2;
    }
    
    /**
     *  引入自定义的字体
     * @param fontStyle 字体样式
     * @param fontSize  字体大小
     * @return
     */
    public static Font getFont(String fontFilePath, int fontStyle, int fontSize) {
        Font font = null;
        FileInputStream fileInputStream = null;
        try { 
//        	String pathString = FontLoader.class.getResource("/font/xxx.ttf").getFile();
//        	Font dynamicFont = Font.createFont(Font.TRUETYPE_FONT, new File(pathString));
        	
            fileInputStream = new FileInputStream(new File(fontFilePath));
            Font tempFont = Font.createFont(Font.TRUETYPE_FONT, fileInputStream);
            font = new Font(tempFont.getFontName(), fontStyle, fontSize);
        } catch (Exception e) {
			logger.error("fontFilePath: "+ fontFilePath, e);
        } finally {
            try {
                fileInputStream.close();
            } catch (Exception e) {
				logger.error("fontFilePath: "+ fontFilePath, e);
            }
        }
        return font;
    }
    
    public static Color String2Color(String str) {
  	  int i = Integer.parseInt(str.substring(1),16);   
  	  return new Color(i);
  	}

	public static void main(String[] args) {
		String s = ImageUtils.GetImageStr("D:\\home\\111.png");
		String total = "<html><body><img src='data:image/jpeg;base64,"+s+"'/></body></html>";
		System.out.println(total);
	}
}