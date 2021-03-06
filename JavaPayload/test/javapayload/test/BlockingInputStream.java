/*
 * Java Payloads.
 * 
 * Copyright (c) 2010, 2011 Michael 'mihi' Schierl
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *   
 * - Neither name of the copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND THE CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR THE CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javapayload.test;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BlockingInputStream extends FilterInputStream {
	
	boolean wait = false;
	public BlockingInputStream(InputStream in) {
		super(in);
	}	

	public int read() throws IOException {
		int b = wait ? '�' : super.read();
		wait = false;
		while (b == '�') {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
			b = read();
		}
		while (b == -1)
			block();
		return b;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		if (len == 0)
			return 0;
		int c = read();
		while (c == -1)
			block();
		b[off] = (byte) c;
		int i = 1;
		try {
			while (i < len) {
				c = super.read();
				if (c == '�' || c == -1)
				{
					wait = true;
					break;
				}
				b[off + i] = (byte) c;
				i++;
			}
		} catch (IOException ex) {
		}
		return i;
	}

	private synchronized void block() {
		try {
			wait();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
