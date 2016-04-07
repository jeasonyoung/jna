/* Copyright (c) 2010 Daniel Doubrovkine, All Rights Reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */
package com.sun.jna.platform.win32;

import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.Winspool.JOB_INFO_1;
import com.sun.jna.platform.win32.Winspool.PRINTER_INFO_1;
import com.sun.jna.platform.win32.Winspool.PRINTER_INFO_2;
import com.sun.jna.platform.win32.Winspool.PRINTER_INFO_4;
import com.sun.jna.ptr.IntByReference;

/**
 * Winspool Utility API.
 * 
 * @author dblock[at]dblock.org, Ivan Ridao Freitas, Padrus
 */
public abstract class WinspoolUtil {

    public static PRINTER_INFO_1[] getPrinterInfo1() {
        IntByReference pcbNeeded = new IntByReference();
        IntByReference pcReturned = new IntByReference();
        Winspool.INSTANCE.EnumPrinters(Winspool.PRINTER_ENUM_LOCAL, null, 1,
                null, 0, pcbNeeded, pcReturned);
        if (pcbNeeded.getValue() <= 0) {
            return new PRINTER_INFO_1[0];
        }

        PRINTER_INFO_1 pPrinterEnum = new PRINTER_INFO_1(pcbNeeded.getValue());
        if (!Winspool.INSTANCE.EnumPrinters(Winspool.PRINTER_ENUM_LOCAL, null,
                1, pPrinterEnum.getPointer(), pcbNeeded.getValue(), pcbNeeded,
                pcReturned)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        pPrinterEnum.read();

        return (PRINTER_INFO_1[]) pPrinterEnum.toArray(pcReturned.getValue());
    }
    
    public static PRINTER_INFO_2[] getPrinterInfo2() {
		return getPrinterInfo2(Winspool.PRINTER_ENUM_LOCAL);
	}
	
	/**
	 * Returns printers that are physically attached to the local machine as
	 * well as remote printers to which it has a network connection.
	 */
	public static PRINTER_INFO_2[] getAllPrinterInfo2() {
		// When Name is NULL, setting Flags to PRINTER_ENUM_LOCAL | PRINTER_ENUM_CONNECTIONS
		// enumerates printers that are installed on the local machine.
		// These printers include those that are physically attached to the local machine 
		// as well as remote printers to which it has a network connection.
		// See https://msdn.microsoft.com/en-us/library/windows/desktop/dd162692(v=vs.85).aspx
		return getPrinterInfo2(Winspool.PRINTER_ENUM_LOCAL | Winspool.PRINTER_ENUM_CONNECTIONS);
	}
	
	private static PRINTER_INFO_2[] getPrinterInfo2(int flags) {
		IntByReference pcbNeeded = new IntByReference();
		IntByReference pcReturned = new IntByReference();
		Winspool.INSTANCE.EnumPrinters(flags, null, 2, null, 0, pcbNeeded, pcReturned);
		if (pcbNeeded.getValue() <= 0)
			return new PRINTER_INFO_2[0];

		PRINTER_INFO_2 pPrinterEnum = new PRINTER_INFO_2(pcbNeeded.getValue());
		if (!Winspool.INSTANCE.EnumPrinters(flags, null, 2, pPrinterEnum.getPointer(), pcbNeeded.getValue(), pcbNeeded,
				pcReturned))
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());

		pPrinterEnum.read();
		return (PRINTER_INFO_2[]) pPrinterEnum.toArray(pcReturned.getValue());
	}

	public static PRINTER_INFO_2 getPrinterInfo2(String printerName) {
		IntByReference pcbNeeded = new IntByReference();
		IntByReference pcReturned = new IntByReference();
		HANDLEByReference pHandle = new HANDLEByReference();

		if (!Winspool.INSTANCE.OpenPrinter(printerName, pHandle, null))
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());

		Win32Exception we = null;
		PRINTER_INFO_2 pinfo2 = null;

		try {
			Winspool.INSTANCE.GetPrinter(pHandle.getValue(), 2, null, 0, pcbNeeded);
			if (pcbNeeded.getValue() <= 0)
				return new PRINTER_INFO_2();

			pinfo2 = new PRINTER_INFO_2(pcbNeeded.getValue());
			if (!Winspool.INSTANCE.GetPrinter(pHandle.getValue(), 2, pinfo2.getPointer(), pcbNeeded.getValue(), pcReturned))
				throw new Win32Exception(Kernel32.INSTANCE.GetLastError());

			pinfo2.read();
		} catch (Win32Exception e) {
			we = e;
		} finally {
			if (!Winspool.INSTANCE.ClosePrinter(pHandle.getValue())) {
				Win32Exception ex = new Win32Exception(Kernel32.INSTANCE.GetLastError());
				if (we != null) {
					ex.addSuppressed(we);
				}
			}
		}

		if (we != null) {
			throw we;
		}

		return pinfo2;
	}

    public static PRINTER_INFO_4[] getPrinterInfo4() {
        IntByReference pcbNeeded = new IntByReference();
        IntByReference pcReturned = new IntByReference();
        Winspool.INSTANCE.EnumPrinters(Winspool.PRINTER_ENUM_LOCAL, null, 4,
                null, 0, pcbNeeded, pcReturned);
        if (pcbNeeded.getValue() <= 0) {
            return new PRINTER_INFO_4[0];
        }

        PRINTER_INFO_4 pPrinterEnum = new PRINTER_INFO_4(pcbNeeded.getValue());
        if (!Winspool.INSTANCE.EnumPrinters(Winspool.PRINTER_ENUM_LOCAL, null,
                4, pPrinterEnum.getPointer(), pcbNeeded.getValue(), pcbNeeded,
                pcReturned)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        pPrinterEnum.read();

        return (PRINTER_INFO_4[]) pPrinterEnum.toArray(pcReturned.getValue());
    }

    public static JOB_INFO_1[] getJobInfo1(HANDLEByReference phPrinter) {
        IntByReference pcbNeeded = new IntByReference();
        IntByReference pcReturned = new IntByReference();
        Winspool.INSTANCE.EnumJobs(phPrinter.getValue(), 0, 255, 1, null, 0,
                pcbNeeded, pcReturned);
        if (pcbNeeded.getValue() <= 0) {
            return new JOB_INFO_1[0];
        }

        JOB_INFO_1 pJobEnum = new JOB_INFO_1(pcbNeeded.getValue());
        if (!Winspool.INSTANCE.EnumJobs(phPrinter.getValue(), 0, 255, 1,
                pJobEnum.getPointer(), pcbNeeded.getValue(), pcbNeeded,
                pcReturned)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        pJobEnum.read();

        return (JOB_INFO_1[]) pJobEnum.toArray(pcReturned.getValue());
    }

}