import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static class Aeroporto{
        public String initials;
        public String name;
        public String city;
        public String state;
        public Double latitude;
        public Double longitude;
        public int id;

        public int prescedente;
        public double estimativa;
        public boolean stt;
        public Aeroporto() {
            stt=true;
            prescedente=-1;
            estimativa=999999999;
        }
    }
    public static class Aresta{
        public int no1;
        public int no2;
        double comprimento;
    }

    public static class Metodos{
        public double Distancia( double lat1 , double lon1 , double lat2 , double lon2){
            lat1 = Math.toRadians(lat1);
            lat2 = Math.toRadians(lat2);
            lon1 = Math.toRadians(lon1);
            lon2 = Math.toRadians(lon2);

            double delta1 = Math.abs(lat1 - lat2);
            double delta2 = Math.abs(lon1-lon2);

            double a = Math.pow(Math.sin(delta1/2),2)+
                    + Math.cos(lat1)*(Math.cos(lat2))*(Math.pow(Math.sin(delta2/2),2));
            return 2*6.371*(Math.asin(Math.pow(a,0.5)));

        }
        public Aeroporto IdentificarPorInitials(ArrayList<Aeroporto> ListaAeroportos ,String Initials){
            int i=0;
            while(i<ListaAeroportos.size()){
                if(ListaAeroportos.get(i).initials.equals(Initials)){
                    return ListaAeroportos.get(i);
                }
                i++;
            }
            return null;
        }

        public void Disktra(ArrayList<Aeroporto> ListaAeroportos ,ArrayList<Aresta> Arestas , int Partida , int Destino){

            for(int i=0;i<ListaAeroportos.size();i++) {
                ListaAeroportos.get(i).estimativa = 99999999;
                i++;
            }
            ListaAeroportos.get(Partida).estimativa=0;
            ListaAeroportos.get(Partida).prescedente=ListaAeroportos.get(Partida).id;

            for(int k = 0; k<ListaAeroportos.size(); k++) {

                //achar vertice de estimativa minima
                int noDeEstMin = -1;
                double EstMin = 999999999;
                for (int i = 0; i < ListaAeroportos.size(); i++) {
                    if (ListaAeroportos.get(i).estimativa < EstMin && ListaAeroportos.get(i).stt == true) {
                        noDeEstMin = ListaAeroportos.get(i).id;
                        EstMin = ListaAeroportos.get(noDeEstMin).estimativa;
                    }
                }

                //re-calcular
                for (int i = 0; i < ListaAeroportos.size(); i++) {
                    for (int j = 0; j < Arestas.size(); j++) {
                        if (((Arestas.get(j).no1 == ListaAeroportos.get(noDeEstMin).id && Arestas.get(j).no2 == ListaAeroportos.get(i).id) || (Arestas.get(j).no2 == ListaAeroportos.get(noDeEstMin).id && Arestas.get(j).no1 == ListaAeroportos.get(i).id)) && ListaAeroportos.get(i).stt == true && i != noDeEstMin) {
                            if (Arestas.get(j).comprimento + ListaAeroportos.get(noDeEstMin).estimativa < ListaAeroportos.get(i).estimativa) {
                                ListaAeroportos.get(i).estimativa = Arestas.get(j).comprimento + ListaAeroportos.get(noDeEstMin).estimativa;
                                ListaAeroportos.get(i).prescedente = ListaAeroportos.get(noDeEstMin).id;
                                break;
                            }
                        }
                    }
                }
                //fechar no visitado
                ListaAeroportos.get(noDeEstMin).stt = false;
            }
        }
        public void Mostar(ArrayList<Aeroporto> ListaAeroportos){
            System.out.println("Iniciais"+"|    "+"Estado"+"   |    "+"Nome");
            int i=0;
            while(i<ListaAeroportos.size()){
                Aeroporto aer = ListaAeroportos.get(i);
                System.out.println(aer.initials+"   |    "+aer.state+"   |    "+aer.name);
                i++;
            }
        };
        public void RotaOtima(ArrayList<Aeroporto> ListaAeroportos ,ArrayList<Aresta> Arestas , String Partida , String Destino){

            Metodos Met = new Metodos();

            int no1;
            int no2;
            no1 =  Met.IdentificarPorInitials(ListaAeroportos,Partida).id;
            no2 =  Met.IdentificarPorInitials(ListaAeroportos,Destino).id;


            //Remocao da aresta direta
            int i = 0;
            while(true){
                if(  (  Arestas.get(i).no1==no1 && Arestas.get(i).no2==no2  ) ||  (  Arestas.get(i).no1==no2 && Arestas.get(i).no2==no1  )  ){
                    Arestas.remove(i);
                    break;
                }
                i++;
            }


            //Disktra
            Met.Disktra(ListaAeroportos , Arestas , no1, no2);

            //Imprimir Rota
            int cidadeAnterior = no2;
            String rota=ListaAeroportos.get(cidadeAnterior).initials;

            while (cidadeAnterior != no1){
                cidadeAnterior = ListaAeroportos.get(cidadeAnterior).prescedente;
                rota = ListaAeroportos.get(cidadeAnterior).initials +"->"+ rota;

            }
            System.out.println("Rota Ótima : " + rota);
            System.out.println("Distancia Minima: " + ListaAeroportos.get(no2).estimativa *1000 +"km");
        }
    }





    public static void main(String[] args) {

        Scanner ler = new Scanner(System.in);

        Metodos Met = new Metodos();

        ArrayList<Aeroporto> ListaAeroportos = new ArrayList<Aeroporto>();
        ArrayList<Aresta> Arestas = new ArrayList<Aresta>();
        int Count = 0 ;


//Obter Aeroportos do Banco de Dados
        try {

            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/projetojava","root","12345678");
            Statement sttm = con.createStatement();
            ResultSet resset = sttm.executeQuery("select * from aeroportos2");


            while (resset.next()) {
                Aeroporto aux = new Aeroporto();
                aux.initials = resset.getString("initials");
                aux.name = resset.getString("name");
                aux.city = resset.getString("city");
                aux.state = resset.getString("state");
                aux.latitude = resset.getDouble("latitude");
                aux.longitude = resset.getDouble("longitude");
                aux.id = Count;

                ListaAeroportos.add(aux);
                Count++;
            }
        } catch( Exception e) {
            e.printStackTrace();
        }
//Cadastro das Arestas em uma Lista
        for(int i=0; i<Count;i++){
            for(int j=0; j<i;j++){
                Aresta aux = new Aresta();
                aux.no1 = i;
                aux.no2 = j;
                aux.comprimento = Met.Distancia(ListaAeroportos.get(i).latitude , ListaAeroportos.get(i).longitude , ListaAeroportos.get(j).latitude , ListaAeroportos.get(j).longitude);
                Arestas.add(aux);
            }
        }

        Met.Mostar(ListaAeroportos);
        System.out.println("Digite a iniciais doas aeroportos de origem e destino respectivamente:");
        String Partida = ler.nextLine();
        String Destino = ler.nextLine();
        Met.RotaOtima(ListaAeroportos , Arestas ,Partida , Destino);



        try {

            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/projetojava","root","12345678");
            Statement sttm = con.createStatement();
            sttm.executeUpdate("insert into resultados (Partida,EscalaOtima,Destino,Distancia) values ('"+Partida+"','"+Destino+"','"+
                    ListaAeroportos.get(Met.IdentificarPorInitials(ListaAeroportos,Destino).prescedente).initials
                    +"','"+
                    ListaAeroportos.get(Met.IdentificarPorInitials(ListaAeroportos,Destino).id).estimativa * 1000
                    +"');");

        } catch( Exception e) {
            e.printStackTrace();
        }
    }
}