//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        HashMapImplementation<Integer,String> hash=new HashMapImplementation();
//        hash.put(1,"one");
//        hash.put(2,"two");
//        hash.put(3,"three");
//        hash.put(2,"twotwo");
//        hash.put(null,"nil");
//        hash.put(null,"nill");
//        hash.put(0,"zero");
//
//
//
//
//        System.out.println(hash.get(1));
//        System.out.println(hash.get(2));
//        System.out.println(hash.get(0));
//        System.out.println(hash.get(null));
//
//        System.out.println();
//        System.out.println(hash.get(3));
//        hash.remove(3);
//        System.out.println(hash.get(3));
        int counter=0;
        for(int i=0;i<20;i++){
            hash.put(counter,""+counter);
            counter++;
        }
        System.out.println(hash.getCapacity());

    }
}