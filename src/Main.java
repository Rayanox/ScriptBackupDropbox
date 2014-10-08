import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

import javax.print.attribute.standard.Compression;

//TODO Modifs à faire: 
//	-> Dans les logs et dans les noms de fichiers, rajouter +1 aux mois car ils commencent à 0 et non pas à 1.
//  -> Gérer la manipulation des fichiers malgré la présence de .txt dans le meme dossier.
//  -> Faire des tests sur sa machine pour voir si la limite de backups est bien respectée...

public class Main {

	/**
	 * @param args
	 */
	public static String pathDropbox = System.getProperty("user.home")+"/.face";//"/Dropbox";
	public static String pathBackupFolder = System.getProperty("user.home")+"/.BackupDropbox";
	
	public static String LogPath = pathBackupFolder+"/"+"Log.txt";
	
	private static final int limiteNbBackupsMax = 3;
	
	
	public static void main(String[] args) {
		try{
			initFolders();
		}catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}
		
		
		changerLesOrdresNumerosFichiers(new File(pathBackupFolder));
		String nomFichier = BuildNomFichier();
		
		LoggerHeure(true);
		//activationDropbox(false);
		compresser(nomFichier);
		//activationDropbox(true);
		LoggerHeure(false);

	}
	
	private static void activationDropbox(boolean activer) {
		String cmd = activer ? "dropbox start" : "dropbox stop";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void LoggerHeure(boolean debut) {
		File f = new File(LogPath);
		try {
			FileWriter writer = new FileWriter(f, true);
			Calendar cal = Calendar.getInstance();
			String date = cal.get(cal.DAY_OF_MONTH)+"/"+cal.get(cal.MONTH)+"/"+cal.get(cal.YEAR)+" à "+cal.get(cal.HOUR_OF_DAY)+"h"+cal.get(cal.MINUTE)+"min"+cal.get(cal.SECOND)+"sec";
			writer.write((debut ? "- Début: " : "- Fin: ")+date+"\r\n"+(!debut ? "\r\n" : ""));
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void changerLesOrdresNumerosFichiers(File f) {
		
		
		for (File fils : f.listFiles()) {
			if(fils.getName().toLowerCase().endsWith(".tar.gz")) {
				String [] nomDecompose = fils.getName().split("_");
				int ancienNum = ParseInt(nomDecompose[0]);
				if(ancienNum != -1) {
					int newNum = ancienNum +1;
					if(newNum <= limiteNbBackupsMax) {
						String nouveauNom = newNum+"_";
						for(int i=1; i< nomDecompose.length; i++) {
							nouveauNom+= nomDecompose[i] + '_';
						}
						nouveauNom = nouveauNom.substring(0, nouveauNom.length()-1);
						boolean renamed = fils.renameTo(new File(fils.getParentFile().getAbsolutePath()+"/"+nouveauNom));
						System.out.println(fils.getAbsolutePath()+"/"+nouveauNom);
					}else {
						fils.delete();
					}				
				}
			}			
		}
	}

	private static void compresser(String nomFichier) {
		String exec = "tar -zcvf "+pathBackupFolder+"/"+nomFichier+(nomFichier.endsWith(".tar.gz") ? "" : ".tar.gz")+" "+pathDropbox;
		try {
			Process p = Runtime.getRuntime().exec(exec);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while((line = reader.readLine()) != null);
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String BuildNomFichier() {
		Calendar cal = Calendar.getInstance();
		String date = cal.get(cal.DAY_OF_MONTH)+"-"+(cal.get(cal.MONTH)+1)+"-"+cal.get(cal.YEAR);
		String nom = "1_Backup-Dropbox___"+date;
		return nom;
	}


	private static int ParseInt(String nom) {
		for(int i=0; i<nom.length(); i++) {
			int carac = (int) nom.charAt(i);
			if(carac < 48 || carac > 57) {
				return i == 0 ? -1 : Integer.parseInt(nom.substring(0, i));
			}
		}
		return Integer.parseInt(nom);
	}

	private static void initFolders() throws Exception {
		File f = new File(pathDropbox);
		if(!f.exists())
			throw new Exception("Dossier Dropbox inexistant ! Attendu à l'endroit : "+pathDropbox);
		
		f = new File(pathBackupFolder);
		f.mkdirs();
	}
	
	

}
