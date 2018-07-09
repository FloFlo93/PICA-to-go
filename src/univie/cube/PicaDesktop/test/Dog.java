package univie.cube.PicaDesktop.test;

public class Dog {
	private static Dog instance;
	
	private Dog() {}
	
	public static Dog getInstance() {
		if(instance == null) Dog.instance = new Dog();
		return Dog.instance;
	}
}






