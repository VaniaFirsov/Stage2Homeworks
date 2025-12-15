public class HashMapImplementation<K,V> {
    private Node<K,V>[] buckets;
    private int size;
    private int capacity;

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    private static class Node<K,V>{
        K key;
        V value;
        Node<K,V> next;
        Node(K key, V value){
            this.key=key;
            this.value=value;
        }
    }
    HashMapImplementation(){
        this.capacity=16;
        this.size=0;
        this.buckets=new Node[capacity];
    }
    //ищем значение по ключу
    public V get(K key){
        int keyIndex=0;
        if(key==null){
            //если key==null, то индекс это 0
        }else{
            keyIndex=performHash(key);
        }
        Node<K,V> keyNode=buckets[keyIndex];
        while(keyNode!=null){
            if(key==null){
                if(keyNode.key==null){
                    return keyNode.value;
                }
            }
            else if(keyNode.key!=null && keyNode.key.equals(key)){
                    return keyNode.value;
            }
            keyNode=keyNode.next;
        }
        return null;
    }
    //добавляем ноду, если уже есть такой ключ
    //то меняем значение
    public V put(K key, V value){
        if((double) size/capacity>=0.75){
            extendCapacity();
        }
        int keyIndex=0;
        if(key==null){
            //если key==null, то индекс это 0
        }else{
            keyIndex=performHash(key);
        }
        //проверяем, нет ли в бакете элемента с таким же ключом
        Node<K,V> keyNode=buckets[keyIndex];
        while(keyNode!=null){
            if(key==null){
                if(keyNode.key==null){
                    V oldValue=keyNode.value;
                    keyNode.value=value;
                    return oldValue;
                }
            }
            else if(keyNode.key!=null && keyNode.key.equals(key)){
                //меняем значнеие
                V oldValue=keyNode.value;
                keyNode.value=value;
                return oldValue;
            }
            keyNode=keyNode.next;
        }
        //добавляем новую ноду в бакет
        Node<K,V> newNode=new Node<>(key,value);
        newNode.next=buckets[keyIndex];
        buckets[keyIndex]=newNode;
        size++;
        return null;
    }
    private void extendCapacity(){
        int newCapacity=this.capacity*2;
        Node<K,V>[] newBuckets=new Node[newCapacity];
        for(int i=0;i<capacity;i++){
            Node<K,V> current=buckets[i];
            while(current!=null){
                Node<K,V> next=current.next;
                int newIndex=0;
                if(current.key==null){

                }else{
                    newIndex=Math.abs(current.key.hashCode())%newCapacity;
                }
                current.next=newBuckets[newIndex];
                newBuckets[newIndex]=current;
                current=next;

            }
        }
        buckets=newBuckets;
        this.capacity=newCapacity;
    }

    //удалем ноду по ключу
    public V remove(K key){
        int keyIndex=0;
        if(key==null){
            //если key==null, то индекс это 0
        }else{
            keyIndex=performHash(key);
        }
        Node<K,V> current=buckets[keyIndex];
        Node<K,V> previous=null;
        while(current!=null){
            if(key==null){
                if(current.key==null){
                    if(previous==null){
                        buckets[keyIndex]=current.next;
                    }else{
                        previous.next=current.next;
                    }
                    size--;
                    return current.value;
                }
            }

            else if(current.key!=null && current.key.equals(key)){
                if(previous==null){
                    buckets[keyIndex]=current.next;
                }else{
                    previous.next=current.next;
                }
                size--;
                return current.value;
            }
            previous=current;
            current=current.next;
        }
        return null;
    }
    private int performHash(K key){
        int keyIndex=Math.abs(key.hashCode())%capacity;
        return keyIndex;
    }
}
