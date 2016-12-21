package wenjh.akit.demo.people.model;

public class UserGender {

	private int gender; // 0 for male, 1 for female
	private String gender_disp = "";// gender for display
	
	public int getGender_orig() {
		return gender;
	}

	public void setGender_orig(int gender_orig) {
		this.gender = gender_orig;
	}

	public String getGender_disp() {
		return gender_disp;
	}

	public void setGender_disp(String gender_disp) {
		this.gender_disp = gender_disp;
	}
	
	public boolean isMale() {
		return gender == 0;
	}
	

}
