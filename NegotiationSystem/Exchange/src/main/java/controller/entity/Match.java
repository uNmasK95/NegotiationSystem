package controller.entity;

import co.paralleluniverse.actors.ActorRef;

public class Match {
  private float preco; //Preco medio
  private String empresa;
  private int quantidade;
  private ActorRef compradorRef;
  private ActorRef vendedorRef;
  private String comprador;
  private String vendedor;

  public Match(float preco, String empresa, int quantidade,
               ActorRef compradorRef, ActorRef vendedorRef, String comprador, String vendedor) {
    this.preco = preco;
    this.empresa = empresa;
    this.quantidade = quantidade;
    this.compradorRef = compradorRef;
    this.vendedorRef = vendedorRef;
    this.comprador = comprador;
    this.vendedor = vendedor;
  }

  public float getPreco() {
    return preco;
  }

  public String getEmpresa() {
    return empresa;
  }

  public int getQuantidade() {
    return quantidade;
  }

  public void setQuantidade(int quantidade) {
    this.quantidade = quantidade;
  }

  public ActorRef getCompradorRef() {
    return compradorRef;
  }

  public ActorRef getVendedorRef() {
    return vendedorRef;
  }

  public String getComprador() {
    return comprador;
  }

  public String getVendedor() {
    return vendedor;
  }
}

