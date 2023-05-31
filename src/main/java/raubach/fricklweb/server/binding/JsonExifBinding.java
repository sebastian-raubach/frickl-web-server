package raubach.fricklweb.server.binding;

import com.google.gson.*;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import raubach.fricklweb.server.computed.Exif;

import java.sql.*;
import java.util.Objects;

/**
 * @author Sebastian Raubach
 */
public class JsonExifBinding implements Binding<JSON, Exif>
{
	@Override
	public Converter<JSON, Exif> converter()
	{
		Gson gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd HH:mm:ss")
				.create();
		return new Converter<>()
		{
			@Override
			public Exif from(JSON o)
			{
				return o == null ? null : gson.fromJson(Objects.toString(o), Exif.class);
			}

			@Override
			public JSON to(Exif o)
			{
				return o == null ? null : JSON.json(gson.toJson(o));
			}

			@Override
			public Class<JSON> fromType()
			{
				return JSON.class;
			}

			@Override
			public Class<Exif> toType()
			{
				return Exif.class;
			}
		};
	}

	@Override
	public void sql(BindingSQLContext<Exif> ctx)
			throws SQLException
	{
		// Depending on how you generate your SQL, you may need to explicitly distinguish
		// between jOOQ generating bind variables or inlined literals.
		if (ctx.render().paramType() == ParamType.INLINED)
			ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("");
		else
			ctx.render().sql("?");
	}

	@Override
	public void register(BindingRegisterContext<Exif> ctx)
			throws SQLException
	{
		ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
	}

	@Override
	public void set(BindingSetStatementContext<Exif> ctx)
			throws SQLException
	{
		ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
	}

	@Override
	public void set(BindingSetSQLOutputContext<Exif> ctx)
			throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void get(BindingGetResultSetContext<Exif> ctx)
			throws SQLException
	{
		ctx.convert(converter()).value(JSON.json(ctx.resultSet().getString(ctx.index())));
	}

	@Override
	public void get(BindingGetStatementContext<Exif> ctx)
			throws SQLException
	{
		ctx.convert(converter()).value(JSON.json(ctx.statement().getString(ctx.index())));
	}

	@Override
	public void get(BindingGetSQLInputContext<Exif> ctx)
			throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}
}
