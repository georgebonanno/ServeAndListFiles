import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;

public class ListFilesWithShell {

	private ListFilesWithShell(){

	}

	public static File[] listFilesWithShell(File dir) throws IOException,InterruptedException {
		if (dir == null || !dir.isDirectory()) {
			throw new IllegalStateException("not a directory: "+dir);
		}

		try {
			ProcessBuilder listFileProcessBuilder = new ProcessBuilder(Arrays.asList("ls","-1",dir.getAbsolutePath()));
			Process listFileProcess=listFileProcessBuilder.start();

			List<File> listedFileList = new ArrayList<>();
			int exitValue=listFileProcess.waitFor();

			if (exitValue == 0) {
				File[] listedFiles;
				try (BufferedReader fileListReader = 
					new BufferedReader(new InputStreamReader(listFileProcess.getInputStream()))) {
					String fileName;
					while ((fileName=fileListReader.readLine()) != null) {
						String fullPath=dir.getAbsolutePath()+"/"+fileName;
						listedFileList.add(new File(fullPath));
					}
				}
			}
			return (File[]) listedFileList.toArray(new File[0]);
		} catch (Exception e) {
			throw new RuntimeException("error while listing files in "+dir);
		}
		
	}

	public static void main(String [] args) throws Exception {
		if (args == null || args.length < 1) {
			System.out.println("usage: <path of folder to list>");
		} else {
			for (int i=0; i<1000000;i++) {
			File [] files = ListFilesWithShell.listFilesWithShell(new File(args[0]));
				for (File f : files) {
					System.out.println(f.getAbsolutePath());
				}
			}
		}
		System.out.println("ready. Press enter");
		System.in.read();
	}
}