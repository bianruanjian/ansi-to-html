package com.bianruanjian.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 描述：把包含 ansi 的命令行文本转为 html 可识别的文本。
 * 
 * @author byg
 * @date 2018年1月24日 上午11:41:51
 * @sinces 0.0.1
 */
public class AnsiToHtml {

	/**
	 * 主要是处理换行符、前景颜色、背景颜色、字体
	 * 
	 * @param ansiText
	 * @return
	 * @throws IOException 
	 */
	public static String execute(String ansiText) throws IOException {
		if (StringUtils.isBlank(ansiText)) {
			return "";
		}
		
		//完整的例子如:ANSI_BOLD_BLACK 对应为 \033[30;1m。但  \033 被命令行识别为类似 '<-' 的箭头符号，在代码中却又无法识别，因此又显示为''
		//在这里处理这个特殊字符，按照规范应该是 \e
		ansiText = ansiText.replaceAll("", "\\\\e");
		
		//\e[0K 表示 reset
		ansiText = ansiText.replaceAll("\\\\e\\[0K", "");

		String aline = null;
		InputStream is = new ByteArrayInputStream(ansiText.getBytes());
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while (true) {
			aline = br.readLine();
			if (aline == null) {
				break;
			} else {
				sb.append(convert(aline)).append("<br>");
			}
		}

		if (StringUtils.isNotBlank(sb.toString())) {
			sb.delete(sb.length() - 4, sb.length());
		}

		return sb.toString();
	}

	public static String convert(String text) {
		String patt = "\\\\e(.*?)(\\[.*?m)";

		Pattern r = Pattern.compile(patt);
		Matcher m = r.matcher(text);
		String ascii = null;
		StringBuffer sb = new StringBuffer();
		boolean tagOpen = false;
		ColorMemory baseColor = new ColorMemory();
		while (m.find()) {
			ascii = m.group(2);
			ascii = ascii.substring(1);
			StringBuilder classSb = new StringBuilder();
			if (ascii.contains(";")) {
				String[] codes = ascii.split(";");
				String htmlClass = "";
				for (String code : codes) {
					if (code.endsWith("m")) {
						code = code.substring(0, code.length() - 1);
					}
					if (StringUtils.isBlank(code)) {
						code = "0";
					}
					Color color = Color.getColorByCode(Integer.valueOf(code));
					if (color == null) {
						m.appendReplacement(sb, "");
						tagOpen = false;
						continue;
					}
					baseColor.set(color);
					htmlClass = color.getHtmlClass() + " ";
				}
				if (htmlClass.endsWith(" ")) {
					htmlClass = htmlClass.substring(0, htmlClass.length() - 1);
				}
				classSb.append(baseColor.getHtmlClass()).append(" ").append(htmlClass).append(" ");
			} else {
				if (ascii.endsWith("m")) {
					ascii = ascii.substring(0, ascii.length() - 1);
				}
				Color color = Color.getColorByCode(Integer.valueOf(ascii));
				if (color == null) {
					m.appendReplacement(sb, "");
					tagOpen = false;
					continue;
				}
				baseColor.set(color);
				if (color.getCode() == Color.reset.getCode() || color.getCode() == Color.font_bold_off.getCode()
						|| color.getCode() == Color.font_italic_off.getCode()
						|| color.getCode() == Color.font_underline_off.getCode()
						|| color.getCode() == Color.font_conceal_off.getCode()
						|| color.getCode() == Color.font_cross_off.getCode()) {
					classSb.delete(0, classSb.length());
					baseColor.set(Color.reset);
				}

				ascii = baseColor.getHtmlClass() + " " + color.getHtmlClass();
				classSb.append(ascii).append(" ");
			}

			if ("".equals(classSb.toString().trim())) {
				m.appendReplacement(sb, "");
				if (tagOpen) {
					sb.append("</span>");
				}
			} else {
				if (classSb.toString().startsWith(" ")) {
					classSb.delete(0, 1);
				}
				if (classSb.toString().endsWith(" ")) {
					classSb.delete(classSb.length() - 1, classSb.length());
				}

				// 判断是否需要拼上结束符
				if (StringUtils.isNotBlank(sb.toString().trim()) && sb.toString().contains("<span")
						&& !sb.toString().endsWith("</span>")) {
					m.appendReplacement(sb, "</span><span class=\"" + classSb.toString() + "\">");
				} else {
					m.appendReplacement(sb, "<span class=\"" + classSb.toString() + "\">");
					tagOpen = true;
				}
			}

		}

		if (tagOpen && sb.toString().endsWith("</span>")) {
			tagOpen = false;
		}

		m.appendTail(sb);
		if (tagOpen) {
			sb.append("</span>");
		}
		return sb.toString();

	}
	
}

enum Color {
	//根据 ANSI_escape_code 规则实现: https://en.wikipedia.org/wiki/ANSI_escape_code
	//0,39,49都比较特殊，0代表reset，终结符，39代表默认前景色，49代表默认背景色
	
	//rest 结束
	reset(0, ""),
	
	//字体
	font_bold(1, "bold"),
	font_italic(3, "italic"),
	font_underline(4, "underline"),
	font_conceal(8, "conceal"),
	font_cross(9, "cross"),

	font_bold_off(21, ""),//Bold off or Double Underline
	font_normal_off(21, ""),
	font_italic_off(23, ""),
	font_underline_off(24, ""),
	font_conceal_off(28, ""),
	font_cross_off(29, ""),
	
    //前景色
	fg_black(30, "black"),
	fg_red(31, "red"),
	fg_green(32, "green"),
	fg_yellow(33, "yellow"),
	fg_blue(34, "blue"),
	fg_magenta(35, "magenta"),
	fg_cyan(36, "cyan"),
	fg_white(37, "white"),
	fg_256(38, ""),
	fg_default(39, ""),
	
	//背景色
	bg_black(40, "black"),
	bg_red(41, "red"),
	bg_green(42, "green"),
	bg_yellow(43, "yellow"),
	bg_blue(44, "blue"),
	bg_magenta(45, "magenta"),
	bg_cyan(46, "cyan"),
	bg_white(47, "white"),
	bg_256(48, ""),
	bg_fefault(49, "");
	
	private Color(int code, String classText) {
		this.code = code;
		this.classText = classText;
	}

	private int code;
	private String classText;
	
	String fgColor = "";
	String bgColor = "";
	String fontStyle = "";

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getClassText() {
		return classText;
	}

	public void setClassText(String classText) {
		this.classText = classText;
	}

	public static Color getColorByCode(int code) {
		for (Color color : Color.values()) {
			if (color.getCode() == code) {
				return color;
			}
		}
		return null;
	}

	public String getFgColor() {
		return fgColor;
	}

	public void setFgColor(String fgColor) {
		this.fgColor = fgColor;
	}

	public String getBgColor() {
		return bgColor;
	}

	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}

	public String getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
	}

	/**
	 * 
	 * @param prefix
	 *            fg,bg
	 * @param color
	 * @return
	 */
	public String getHtmlClass() {
		String htmlClass = "";
		String term = "term";
		String prefix = null;
		if (this.code == 0 || this.code == 39 || this.code == 49) {
			return "";
		} else if (this.code <= 9) {
			prefix = "";
			fontStyle = term + "-" + this.classText;
			return term + "-" + this.classText;
		} else if (this.code < 30) {
			return "";
		} else if (this.code <= 38) {
			prefix = "fg";
			fgColor = term + "-" + prefix + "-" + this.classText;
		} else {
			prefix = "bg";
			bgColor = term + "-" + prefix + "-" + this.classText;
		}

		htmlClass = fgColor + " " + bgColor + " " + fontStyle;

		return htmlClass.trim();
	}
	
}

class ColorMemory {
	String fgColor = "";
	String bgColor = "";
	String fontStyle = "";
	private Color color;

	public String getFgColor() {
		return fgColor;
	}

	public void set(Color color) {
		this.color = color;
		this.color.getHtmlClass();

		if (color.getCode() == Color.reset.getCode()) {
			this.setFgColor("");
			this.setBgColor("");
			this.setFontStyle("");
			return;
		}

		if (StringUtils.isNotBlank(this.color.getFgColor())) {
			this.setFgColor(color.getFgColor());
		}
		if (StringUtils.isNotBlank(this.color.getBgColor())) {
			this.setBgColor(color.getBgColor());
		}
		if (StringUtils.isNotBlank(this.color.getFontStyle())) {
			this.setFontStyle(color.getFontStyle());
		}
	}

	public String getHtmlClass() {
		String memoryHtmlClass = "";
		if (this.color == null) {
			return this.color.getHtmlClass().trim();
		}
		if (StringUtils.isBlank(this.color.getFgColor())) {
			memoryHtmlClass = fgColor;
		}
		if (StringUtils.isBlank(this.color.getBgColor())) {
			memoryHtmlClass = memoryHtmlClass + " " + bgColor;
		}
		if (StringUtils.isBlank(this.color.getFontStyle())) {
			memoryHtmlClass = memoryHtmlClass + " " + fontStyle;
		}
		return memoryHtmlClass.trim();
	}

	public void setFgColor(String fgColor) {
		this.fgColor = fgColor;
	}

	public String getBgColor() {
		return bgColor;
	}

	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}

	public String getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
	}
}

