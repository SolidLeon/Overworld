package com.solidleon.overworld;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Logging {

	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static PrintStream out = System.out;
	private static JFrame logWindow = null;
	private static JTextPane text = null;
	
	private static enum LogLevel {
		INFO(Color.black, Color.white),
		WARNING(Color.yellow.darker(), Color.white),
		ERROR(Color.red.darker(), Color.white),
		CRITICAL(Color.white, Color.red.darker());
		
		public Color fg = Color.white;
		public Color bg = Color.black;
		
		private LogLevel(Color fg, Color bg) {
			this.fg = fg;
			this.bg = bg;
		}

		private LogLevel() {
			this(Color.black, Color.white);
		}
				
	}
	
	private static void log(LogLevel level, String format, Object ...args) {
		String caller = Thread.currentThread().getStackTrace()[3].getMethodName();
		String prefix = String.format("%-10s - %s", Thread.currentThread().getName(), caller);
		String str = String.format("%s %-8s: %-35s %s%n", 
				sdf.format(new Date()), 
				level, 
				prefix, 
				String.format(format, args));
		if (logWindow == null)
			out.printf(str);
		else
			appendText(level.fg, level.bg, str);
		if (level == LogLevel.CRITICAL) {
			System.exit(99);
		}
	}
	
	public static void info(String format, Object ...args) {
		log(LogLevel.INFO, format, args);
	}
	
	public static void error(String format, Object ...args) {
		log(LogLevel.ERROR, format, args);
	}
	
	public static void warning(String format, Object ...args) {
		log(LogLevel.WARNING, format, args);
	}

	public static void exception(Throwable e, String format, Object ...args) {
		log(LogLevel.ERROR, "Exception caugt '%s'", e.getMessage());
		if (format != null)
			log(LogLevel.ERROR, format, args);
		e.printStackTrace(out);
	}
	public static void exception(Throwable e) {
		exception(e, null);
	}
	
	public static PrintStream getOut() {
		return out;
	}
	public static void setOut(PrintStream out) {
		Logging.out = out;
	}

	public static void openLogWindow() {
		if (logWindow == null) {
			logWindow = new JFrame("Log Window");
			logWindow.setPreferredSize(new Dimension(640, 480));
			logWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
			text = new JTextPane() {
				@Override
				public boolean getScrollableTracksViewportWidth() {
					return getUI().getPreferredSize(this).width <= getParent().getSize().width;
				}
			};
			text.setEditable(false);
			text.setFont(new Font("Monospaced", Font.PLAIN, 12));
			DefaultCaret caret = (DefaultCaret)text.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			out = new PrintStream(new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					try {
						text.getDocument().insertString(text.getDocument().getLength(), String.valueOf((char)b), null);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			});
			logWindow.add(new JScrollPane(text));
			
			logWindow.pack();
			logWindow.setLocationRelativeTo(null);
		}
		SwingUtilities.invokeLater(() -> logWindow.setVisible(true));
	}

	private static void appendText(Color fg, Color bg, String str) {
		SimpleAttributeSet aset = new SimpleAttributeSet();
		StyleConstants.setForeground(aset, fg);
		StyleConstants.setBackground(aset, bg);
		
		int start = text.getDocument().getLength();
		int len = text.getText().length();
		StyledDocument sd = (StyledDocument) text.getDocument();
		sd.setCharacterAttributes(start, len, aset, false);
		

		try {
			text.getDocument().insertString(text.getDocument().getLength(), str, aset);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
