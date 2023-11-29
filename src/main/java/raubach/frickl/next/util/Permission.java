package raubach.frickl.next.util;

public enum Permission
{
	TAG(1),
	IMAGE_UPLOAD(2),
	IMAGE_DELETE(4),
	ALBUM_CREATE(8),
	ALBUM_DELETE(16),
	SETTINGS_CHANGE(32);

	private final int value;

	Permission(int value) {
		this.value = value;
	}

	public int getNumericValue() {
		return value;
	}

	public boolean allows(short check)
	{
		return (check & value) == value;
	}

	public static int getAll() {
		return TAG.value | IMAGE_UPLOAD.value | IMAGE_DELETE.value | ALBUM_CREATE.value | ALBUM_DELETE.value | SETTINGS_CHANGE.value;
	}
}
