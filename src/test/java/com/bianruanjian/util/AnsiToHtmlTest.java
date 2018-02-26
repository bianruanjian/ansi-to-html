package com.bianruanjian.util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * 描述：AnsiToHtml 的测试用例
 * 
 * @author byg
 * @date 2018年1月31日 上午11:01:52
 * @sinces 0.0.1
 */
public class AnsiToHtmlTest {

	@Test
	public void test_non_ansi() throws IOException {
		String shellText = "Hello";
		Assert.assertEquals("Hello", AnsiToHtml.execute(shellText));
	}

	@Test
	public void test_new_line() throws IOException {
		String shellText = "Hello\r\nWorld";
		String expected = "Hello<br>World";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		
		shellText = "Hello\nWorld";
		html = AnsiToHtml.execute(shellText);
		System.err.println(html);
		
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_simple_blue() throws IOException {
		String shellText = "\\e[33mworld";
		String expected = "<span class=\"term-fg-yellow\">world</span>";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_simple_red() throws IOException {
		String shellText = "\\e[31mHello\\e[0m";
		String expected = "<span class=\"term-fg-red\">Hello</span>";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_simple_bold() throws IOException {
		String shellText = "\\e[1m 脚本\\e[21m已运行";
		String expected = "<span class=\"term-bold\"> 脚本</span>已运行";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_simple_red_without_trailing_reset() throws IOException {
		String shellText = "\\e[31mHello";
		String expected = "<span class=\"term-fg-red\">Hello</span>";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_default_on_blue() throws IOException {
		String shellText = "\\e[39;44mHello";
		String expected = "<span class=\"term-bg-blue\">Hello</span>";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_red_on_blue() throws IOException {
		String shellText = "\\e[31;44mHello";
		String expected = "<span class=\"term-fg-red term-bg-blue\">Hello</span>";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_resets_colors_after_red_on_blue() throws IOException {
		String shellText = "\\e[31;44mHello\\e[0m world";
		String expected = "<span class=\"term-fg-red term-bg-blue\">Hello</span> world";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_performs_color_change_from_red_blue_to_yellow_blue() throws IOException {
		String shellText = "\\e[31;44mHello \\e[33mworld";
		String expected_1 = "<span class=\"term-fg-red term-bg-blue\">Hello </span><span class=\"term-bg-blue term-fg-yellow\">world</span>";
		String expected_2 = "<span class=\"term-fg-red term-bg-blue\">Hello </span><span class=\"term-fg-yellow term-bg-blue\">world</span>";
		String html = AnsiToHtml.execute(shellText);
		System.err.println(html);
		boolean valid = false;
		if (expected_1.equals(html)) {
			System.out.println(expected_1);
			valid = true;
		} else if (expected_2.equals(html)) {
			System.out.println(expected_2);
			valid = true;
		} else {
			System.out.println(expected_1);
		}
		Assert.assertTrue(valid);
	}

	@Test
	public void test_performs_color_change_from_red_blue_to_yellow_green() throws IOException {
		String shellText = "\\e[31;44mHello \\e[33;42mworld";
		String expected_1 = "<span class=\"term-fg-red term-bg-blue\">Hello </span><span class=\"term-fg-yellow term-bg-green\">world</span>";
		String expected_2 = "<span class=\"term-fg-red term-bg-blue\">Hello </span><span class=\"term-bg-green term-fg-yellow\">world</span>";
		String html = AnsiToHtml.execute(shellText);
		System.err.println(html);
		boolean valid = false;
		if (expected_1.equals(html)) {
			System.out.println(expected_1);
			valid = true;
		} else if (expected_2.equals(html)) {
			System.out.println(expected_2);
			valid = true;
		} else {
			System.out.println(expected_1);
		}
		Assert.assertTrue(valid);
	}

	@Test
	public void test_performs_color_change_from_red_blue_to_reset_yellow_green() throws IOException {
		String shellText = "\\e[31;44mHello\\e[0m \\e[33;42mworld";
		String expected = "<span class=\"term-fg-red term-bg-blue\">Hello</span> <span class=\"term-fg-yellow term-bg-green\">world</span>";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_ignores_unsupported_codes() throws IOException {
		String shellText = "\\e[51mHello\\e[0m";
		String expected = "Hello";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_bold_text() throws IOException {
		String shellText = "\\e[1mHello";
		String expected = "<span class=\"term-bold\">Hello</span>";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_italic_text() throws IOException {
		String shellText = "\\e[3mHello";
		String expected = "<span class=\"term-italic\">Hello</span>";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_reset_italic_text() throws IOException {
		String shellText = "\\e[3mHello\\e[23m world";
		String expected = "<span class=\"term-italic\">Hello</span> world";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_underlined_text() throws IOException {
		String shellText = "\\e[4mHello";
		String expected = "<span class=\"term-underline\">Hello</span>";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}

	@Test
	public void test_reset_underlined_text() throws IOException {
		String shellText = "\\e[4mHello\\e[24m world";
		String expected = "<span class=\"term-underline\">Hello</span> world";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}
	
	
	@Test
	public void test_clear_text() throws IOException {
		String shellText = "\\e[0KRunning with brj-runner dev (HEAD)";
		String expected = "Running with brj-runner dev (HEAD)";
		String html = AnsiToHtml.execute(shellText);
		System.out.println(expected);
		System.err.println(html);
		Assert.assertEquals(expected, html);
	}
	
	

}
