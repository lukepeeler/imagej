/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.script.editor;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class JTextAreaOutputStream extends OutputStream {
	JTextArea textArea;

	ScheduledExecutorService updater = Executors.newScheduledThreadPool(1);
	Vector<String> queue = new Vector<String>();

	/** Creates a new output stream that prints every 400 ms to the textArea.
	 *  When done, call shutdown() to clean up and finish printing any remaining text. */
	public JTextAreaOutputStream(final JTextArea textArea) {
		this.textArea = textArea;
		updater.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				flushQueue();
			}
		}, 10, 400, TimeUnit.MILLISECONDS);
	}

	public void write(int i) {
		write(Character.toString((char)i));
	}

	public void write(byte[] buffer) {
		write(new String(buffer));
	}

	public void write(byte[] buffer, int off, int len) {
		write(new String(buffer, off, len));
	}

	public void write(final String string) {
		queue.add(string);
	}

	public void flushQueue() {
		ArrayList<String> strings;
		synchronized (queue) {
			if (0 == queue.size()) return;
			strings = new ArrayList<String>();
			strings.addAll(queue);
			queue.clear();
		}

		StringBuilder sb = new StringBuilder();
		for (String s : strings)
			sb.append(s);

		synchronized (textArea) {
			int lineCount = textArea.getLineCount();
			// Eliminate the first 100 lines when reaching 1100 lines:
			if (lineCount > 1100) try {
				textArea.replaceRange("", 0,
					textArea.getLineEndOffset(lineCount - 1000));
			} catch (BadLocationException e) { e.printStackTrace(); }
			textArea.append(sb.toString());
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}

	public void flush() {
		flushQueue();
		textArea.repaint();
	}

	public void close() {
		flush();
		updater.shutdown();
	}

	/** Stop printing services, finishing to print any remaining tasks in the context of the calling thread. */
	public void shutdown() {
		List<Runnable> tasks = updater.shutdownNow();
		if (null == tasks)
			return;
		for (Runnable t : tasks)
			t.run();
	}

	/** Stop printing services immediately, not printing any remaining text. */
	public void shutdownNow() {
		updater.shutdownNow();
	}
}
