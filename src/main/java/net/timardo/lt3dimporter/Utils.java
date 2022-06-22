package net.timardo.lt3dimporter;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

import static com.creativemd.creativecore.common.utils.mc.ColorUtils.*;

public class Utils {
    
    public static void postMessage(ICommandSender receiver, String msg) {
        receiver.sendMessage(new TextComponentString(msg));
    }
    
    public static int multiplyColor(int color1, int color2) {
        return RGBAToInt(getRedDecimal(color1) * getRedDecimal(color2), getGreenDecimal(color1) * getGreenDecimal(color2), getBlueDecimal(color1) * getBlueDecimal(color2), getAlphaDecimal(color1) * getAlphaDecimal(color2));
    }
    
    /**
     * Very simple heap sort implementation working with primitive integer arrays only
     * 
     * @param unsortedList integer array to be sorted
     * @return sorted array of integers
     */
    public static int[] heapSort(int[] unsortedList) {
        int[] heap = new int[unsortedList.length];

        int lastIndex = 0;

        for (int i = 0; i < unsortedList.length; i++) {
            int element = unsortedList[i];
            heap[i] = element;

            int tempIndex = lastIndex++;

            while (tempIndex != 0 && (element > heap[(tempIndex - 1) / 2])) {
                heap[tempIndex] = heap[(tempIndex - 1) / 2];
                tempIndex = (tempIndex - 1) / 2;
                heap[tempIndex] = element;
            }
        }

        int[] sortedList = new int[unsortedList.length];
        
        for (int i = 0; i < heap.length; i++) {
            sortedList[i] = removeRoot(heap, i);
        }

        return sortedList;
    }

    private static int removeRoot(int[] heap, int i) {
        int ret = heap[0];
        int last = 0;
        heap[0] = heap[heap.length - (i + 1)];

        while (true) {
            if (last * 2 + 1 > heap.length - 1 - i) break;

            int max = 0;

            if (last * 2 + 1 == heap.length - 1 - i)
                max = 1;
            else
                max = Math.max(heap[last * 2 + 1], heap[last * 2 + 2]) == heap[last * 2 + 1] ? 1 : 2;

            if (!(heap[last * 2 + max] > heap[last])) break;

            int n = heap[last * 2 + max];
            heap[last * 2 + max] = heap[last];
            heap[last] = n;
            last = last * 2 + max;
        }

        return ret;
    }
}
