package sp.senac.br.reembolso.dtos;

import java.io.Serializable;

import com.google.gson.Gson;

public class RelatorioDto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String dataInicio;
	private String dataFim;
	private String formaPagamento;
	private String unidade;
	private Integer tipoRelatorio;
	private Integer idTipoLancamento;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataFim == null) ? 0 : dataFim.hashCode());
		result = prime * result + ((dataInicio == null) ? 0 : dataInicio.hashCode());
		result = prime * result + ((formaPagamento == null) ? 0 : formaPagamento.hashCode());
		result = prime * result + ((idTipoLancamento == null) ? 0 : idTipoLancamento.hashCode());
		result = prime * result + ((tipoRelatorio == null) ? 0 : tipoRelatorio.hashCode());
		result = prime * result + ((unidade == null) ? 0 : unidade.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelatorioDto other = (RelatorioDto) obj;
		if (dataFim == null) {
			if (other.dataFim != null)
				return false;
		} else if (!dataFim.equals(other.dataFim))
			return false;
		if (dataInicio == null) {
			if (other.dataInicio != null)
				return false;
		} else if (!dataInicio.equals(other.dataInicio))
			return false;
		if (formaPagamento == null) {
			if (other.formaPagamento != null)
				return false;
		} else if (!formaPagamento.equals(other.formaPagamento))
			return false;
		if (idTipoLancamento == null) {
			if (other.idTipoLancamento != null)
				return false;
		} else if (!idTipoLancamento.equals(other.idTipoLancamento))
			return false;
		if (tipoRelatorio == null) {
			if (other.tipoRelatorio != null)
				return false;
		} else if (!tipoRelatorio.equals(other.tipoRelatorio))
			return false;
		if (unidade == null) {
			if (other.unidade != null)
				return false;
		} else if (!unidade.equals(other.unidade))
			return false;
		return true;
	}

	public RelatorioDto () {
	}
	
	@Override 
	public String toString() { 
	    return new Gson().toJson(this);
	}

	public String getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(String dataInicio) {
		this.dataInicio = dataInicio;
	}

	public String getDataFim() {
		return dataFim;
	}

	public void setDataFim(String dataFim) {
		this.dataFim = dataFim;
	}

	public String getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(String formaPagamento) {
		this.formaPagamento = formaPagamento;
	}

	public String getUnidade() {
		return unidade;
	}

	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}

	public Integer getTipoRelatorio() {
		return tipoRelatorio;
	}

	public void setTipoRelatorio(Integer tipoRelatorio) {
		this.tipoRelatorio = tipoRelatorio;
	}

	public Integer getIdTipoLancamento() {
		return idTipoLancamento;
	}

	public void setIdTipoLancamento(Integer idTipoLancamento) {
		this.idTipoLancamento = idTipoLancamento;
	}

}