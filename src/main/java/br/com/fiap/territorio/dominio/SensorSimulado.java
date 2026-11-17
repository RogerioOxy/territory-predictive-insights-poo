package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Formatador;
import br.com.fiap.territorio.Mensagens;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;

import java.util.Objects;
import java.util.Random;

/**
 * Base abstrata que implementa Sensor e compartilha estado, falhas e controle da simulacao.
 */
public abstract class SensorSimulado implements Sensor {

    public static final double PROBABILIDADE_FALHA_PADRAO = 0.04;
    public static final double PROBABILIDADE_LEITURA_ESPURIA_PADRAO = 0.04;

    private final String codigo;
    private final Random random;
    private final double minimoSimulado;
    private final double maximoSimulado;
    private final double probabilidadeFalha;
    private final double probabilidadeLeituraEspuria;
    private double valorAtual;
    private boolean funcionando = true;

    protected SensorSimulado(String codigo, double valorInicial, double minimoSimulado, double maximoSimulado,
                             Random random, double probabilidadeFalha, double probabilidadeLeituraEspuria) {
        if (probabilidadeFalha < 0 || probabilidadeFalha > 1
                || probabilidadeLeituraEspuria < 0 || probabilidadeLeituraEspuria > 1) {
            throw new IllegalArgumentException(Mensagens.ERRO_PROBABILIDADE_INVALIDA);
        }
        this.codigo = Objects.requireNonNull(codigo);
        this.random = Objects.requireNonNull(random);
        this.minimoSimulado = minimoSimulado;
        this.maximoSimulado = maximoSimulado;
        this.probabilidadeFalha = probabilidadeFalha;
        this.probabilidadeLeituraEspuria = probabilidadeLeituraEspuria;
        this.valorAtual = limitar(valorInicial);
    }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!funcionando) {
            throw new FonteIndisponivelException(codigo,
                    Formatador.f(Mensagens.ERRO_SENSOR_DESLIGADO, codigo, getTipo().getNome()));
        }
        if (random.nextDouble() < probabilidadeFalha) {
            throw new FonteIndisponivelException(codigo,
                    Formatador.f(Mensagens.ERRO_SENSOR_SEM_RESPOSTA, codigo, getTipo().getNome()));
        }
        if (random.nextDouble() < probabilidadeLeituraEspuria) {
            return gerarLeituraEspuria(valorAtual, random);
        }
        valorAtual = limitar(proximoValor(valorAtual, random));
        return valorAtual;
    }

    protected abstract double proximoValor(double atual, Random random);

    protected abstract double gerarLeituraEspuria(double atual, Random random);

    @Override
    public boolean verificarFuncionamento() {
        return funcionando;
    }

    @Override
    public void definirFuncionamento(boolean funcionando) {
        this.funcionando = funcionando;
    }

    @Override
    public void retomarDe(double ultimoValorRegistrado) {
        this.valorAtual = limitar(ultimoValorRegistrado);
    }

    @Override
    public String getCodigo() {
        return codigo;
    }

    private double limitar(double valor) {
        return Math.max(minimoSimulado, Math.min(maximoSimulado, valor));
    }
}
