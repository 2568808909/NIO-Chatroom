package core.ds;

/**
 * 带优先级的节点，用于构建成链表，发送时按优先级发送
 * @param <Item>
 */
public class BytePriorityNode<Item> {
    public byte priority;
    public Item item;
    public BytePriorityNode<Item> next;

    public BytePriorityNode(Item item){
        this.item=item;
    }

    /**
     * 按优先级将节点追加到链表之中
     * @param node
     */
    public void appendWithPriority(BytePriorityNode<Item> node){
        if(next==null){
            next=node;
        }else{
            BytePriorityNode<Item> after=this.next;
            if(after.priority<node.priority){
                node.next=after;
                this.next=node;
            }else{
                after.appendWithPriority(node);
            }
        }
    }
}
